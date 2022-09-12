/*
 * Copyright 2019 Google LLC
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
package com.google.android.ground.persistence.local.room.entity

import androidx.room.*
import com.google.android.ground.model.geometry.Point
import com.google.android.ground.model.mutation.LocationOfInterestMutation
import com.google.android.ground.persistence.local.room.entity.LocationOfInterestEntity.Companion.formatVertices
import com.google.android.ground.persistence.local.room.entity.LocationOfInterestEntity.Companion.parseVertices
import com.google.android.ground.persistence.local.room.models.Coordinates
import com.google.android.ground.persistence.local.room.models.MutationEntitySyncStatus
import com.google.android.ground.persistence.local.room.models.MutationEntityType
import java.util.*
import java8.util.Optional

/**
 * Defines how Room persists LOI mutations for remote sync in the local db. By default, Room uses
 * the name of object fields and their respective types to determine database column names and
 * types.
 */
@Entity(
  tableName = "location_of_interest_mutation",
  foreignKeys =
    [
      ForeignKey(
        entity = LocationOfInterestEntity::class,
        parentColumns = ["id"],
        childColumns = ["location_of_interest_id"],
        onDelete = ForeignKey.CASCADE
      )
    ],
  indices = [Index("location_of_interest_id")]
)
data class LocationOfInterestMutationEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long? = 0,
  @ColumnInfo(name = "survey_id") val surveyId: String,
  @ColumnInfo(name = "type") val type: MutationEntityType,
  @ColumnInfo(name = "state") val syncStatus: MutationEntitySyncStatus,
  @ColumnInfo(name = "retry_count") val retryCount: Long,
  @ColumnInfo(name = "last_error") val lastError: String,
  @ColumnInfo(name = "user_id") val userId: String,
  @ColumnInfo(name = "client_timestamp") val clientTimestamp: Long,
  @ColumnInfo(name = "location_of_interest_id") val locationOfInterestId: String,
  @ColumnInfo(name = "job_id") val jobId: String,
  /** Non-null if the LOI's location was updated, null if unchanged. */
  @Embedded val newLocation: Coordinates?,
  /** Non-empty if a polygon's vertices were updated, null if unchanged. */
  @ColumnInfo(name = "polygon_vertices") val newPolygonVertices: String?
) {

  fun toMutation(): LocationOfInterestMutation {
    return LocationOfInterestMutation.builder()
      .setJobId(jobId)
      .setLocation(Optional.ofNullable(newLocation).map { obj: Coordinates? -> obj!!.toPoint() })
      .setPolygonVertices(parseVertices(newPolygonVertices))
      .setId(id)
      .setSurveyId(surveyId)
      .setLocationOfInterestId(locationOfInterestId)
      .setType(type.toMutationType())
      .setSyncStatus(syncStatus.toMutationSyncStatus())
      .setRetryCount(retryCount)
      .setLastError(lastError)
      .setUserId(userId)
      .setClientTimestamp(Date(clientTimestamp))
      .build()
  }

  companion object {
    fun fromMutation(m: LocationOfInterestMutation): LocationOfInterestMutationEntity =
      LocationOfInterestMutationEntity(
        id = m.id,
        surveyId = m.surveyId,
        locationOfInterestId = m.locationOfInterestId,
        jobId = m.jobId,
        newLocation = m.location.map { point: Point? -> Coordinates.fromPoint(point) }.orElse(null),
        newPolygonVertices = formatVertices(m.polygonVertices),
        type = MutationEntityType.fromMutationType(m.type),
        syncStatus = MutationEntitySyncStatus.fromMutationSyncStatus(m.syncStatus),
        retryCount = m.retryCount,
        lastError = m.lastError,
        userId = m.userId,
        clientTimestamp = m.clientTimestamp.time
      )
  }
}