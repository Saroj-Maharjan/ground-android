/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.groundplatform.android.persistence.remote.firebase.protobuf

import com.google.firebase.firestore.DocumentSnapshot
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.Internal.EnumLite
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import timber.log.Timber

typealias MessageBuilder = GeneratedMessageLite.Builder<*, *>

/**
 * Returns a new instance of the specified [Message] populated with the document's id and data.
 *
 * Corresponding proto fields are matched based on their fields numbers. Several special rules are
 * also applied:
 * * Unrecognized fields and fields of incompatible types are ignored.
 * * If is specified, attempts to set the document ID in the field with [idFieldNumber]. Throws
 *   [IllegalArgumentException] if not found.
 *
 * This implementation is tightly bound to the implementation of code generated by
 * protobuf-kotlin-lite. Future versions of the library may require changes to this util.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Message> KClass<T>.parseFrom(
  documentSnapshot: DocumentSnapshot,
  idFieldNumber: MessageFieldNumber? = null,
): T {
  val builder = newBuilderForType()
  if (idFieldNumber != null) {
    builder.setOrLog(getFieldName(idFieldNumber), documentSnapshot.id)
  }
  documentSnapshot.data.copyInto(builder)
  return builder.build() as T
}

private fun KClass<Message>.parseFrom(map: FirestoreMap?): Message {
  val builder = newBuilderForType()
  map.copyInto(builder)
  return builder.build()
}

private fun FirestoreMap?.copyInto(builder: MessageBuilder) {
  this?.forEach { it.copyInto(builder) }
}

private fun FirestoreMapEntry.copyInto(builder: MessageBuilder) {
  toMessageField(builder::class)?.also { (k, v) ->
    when (v) {
      is MessageMap -> builder.putAllOrLog(k, v)
      is List<*> -> builder.addAllOrLog(k, v)
      else -> builder.setOrLog(k, v)
    }
  }
}

private fun FirestoreMapEntry.toMessageField(
  builderType: KClass<out MessageBuilder>
): MessageField? =
  try {
    val fieldNumber = key.toMessageFieldNumber()
    val fieldName = builderType.getMessageClass().getFieldName(fieldNumber)
    val fieldValue = value.toMessageValue(builderType, fieldName)
    MessageField(fieldName, fieldValue)
  } catch (e: Throwable) {
    Timber.v(e, "Skipping incompatible Firestore value. ${javaClass}: $key=$value")
    null
  }

private fun FirestoreKey.toMessageFieldNumber() =
  toIntOrNull() ?: throw IllegalArgumentException("Non-numeric document key $this")

private fun FirestoreMap.toMessageMap(mapValueType: KClass<*>): MessageMap =
  map { (key: FirestoreValue, value: FirestoreValue) -> key to value.toMessageValue(mapValueType) }
    .toMap()

@Suppress("UNCHECKED_CAST")
private fun FirestoreValue.toMessageValue(
  builderType: KClass<out MessageBuilder>,
  fieldName: MessageFieldName,
): MessageValue {
  val fieldType = builderType.getFieldTypeByName(fieldName)
  return if (fieldType.isSubclassOf(Map::class)) {
    (this as FirestoreMap).toMessageMap(builderType.getMapValueType(fieldName))
  } else if (fieldType.isSubclassOf(List::class)) {
    val elementType = builderType.getListElementFieldTypeByName(fieldName)
    (this as List<FirestoreValue>).map {
      if (elementType.isSubclassOf(GeneratedMessageLite::class)) {
        (elementType as KClass<Message>).parseFrom(it as FirestoreMap)
      } else {
        it.toMessageValue(elementType)
      }
    }
  } else {
    toMessageValue(fieldType)
  }
}

@Suppress("UNCHECKED_CAST", "CognitiveComplexMethod")
private fun FirestoreValue.toMessageValue(targetType: KClass<*>): MessageValue =
  if (targetType == String::class) {
    this as String
  } else if (targetType == Int::class) {
    if (this is Long) {
      this.toInt()
    } else {
      this
    }
  } else if (targetType == Long::class) {
    if (this is Long) {
      this.toLong()
    } else {
      this
    }
  } else if (targetType == Double::class || targetType == Float::class) {
    this
  } else if (targetType == Boolean::class) {
    this as Boolean
  } else if (targetType.isSubclassOf(GeneratedMessageLite::class)) {
    (targetType as KClass<Message>).parseFrom(this as FirestoreMap)
  } else if (targetType.isSubclassOf(EnumLite::class)) {
    var number = this
    if (number is Long) {
      number = number.toInt()
    }
    require(number is Int) { "Expected Int but got ${number::class}" }
    (targetType as KClass<EnumLite>).findByNumber(number)
      ?: throw IllegalArgumentException("Unrecognized enum number $this")
  } else {
    throw UnsupportedOperationException("Unsupported message field type $targetType")
    // TODO: Handle arrays, GeoPoint, int, and other types.
    // Issue URL: https://github.com/google/ground-android/issues/1748
  }
