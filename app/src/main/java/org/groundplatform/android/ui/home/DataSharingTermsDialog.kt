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
package org.groundplatform.android.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.groundplatform.android.ExcludeFromJacocoGeneratedReport
import org.groundplatform.android.R
import org.groundplatform.android.proto.Survey
import org.groundplatform.android.ui.compose.HtmlText
import org.groundplatform.android.ui.theme.AppTheme
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

private fun getResourceAsText(path: String): String =
  object {}.javaClass.getResource(path)?.readText().orEmpty()

@Composable
private fun getDataSharingTermsText(dataSharingTerms: Survey.DataSharingTerms): String =
  when (dataSharingTerms.type) {
    Survey.DataSharingTerms.Type.PRIVATE -> stringResource(R.string.data_sharing_private_message)
    Survey.DataSharingTerms.Type.PUBLIC_CC0 ->
      buildString {
        append(stringResource(R.string.data_sharing_public_message))
        append("\n")
        append(getResourceAsText("/assets/licenses/cc0_license.txt"))
      }
    Survey.DataSharingTerms.Type.CUSTOM -> dataSharingTerms.customText
    else -> stringResource(R.string.data_sharing_no_terms)
  }

@Composable
private fun generateDataSharingTermsHtml(dataSharingTerms: Survey.DataSharingTerms): String {
  val markdownSrc = getDataSharingTermsText(dataSharingTerms)
  val flavor = CommonMarkFlavourDescriptor()
  val parsedTree = MarkdownParser(flavor).buildMarkdownTreeFromString(markdownSrc)
  return HtmlGenerator(markdownSrc, parsedTree, flavor).generateHtml()
}

@Composable
fun DataSharingTermsDialog(
  dataSharingTerms: Survey.DataSharingTerms,
  consentGivenCallback: () -> Unit = {},
) {
  val showDialog = remember { mutableStateOf(true) }

  fun dismissDialog() {
    showDialog.value = false
  }

  if (!showDialog.value) {
    return
  }

  AlertDialog(
    onDismissRequest = { dismissDialog() },
    title = {
      Text(
        text = stringResource(R.string.data_consent_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      val html = generateDataSharingTermsHtml(dataSharingTerms)
      HtmlText(html, Modifier.height(450.dp).fillMaxWidth())
    },
    dismissButton = {
      TextButton(onClick = { dismissDialog() }) { Text(text = stringResource(R.string.cancel)) }
    },
    confirmButton = {
      TextButton(
        onClick = {
          consentGivenCallback()
          dismissDialog()
        }
      ) {
        Text(text = stringResource(R.string.agree_checkbox))
      }
    },
  )
}

@Composable
@Preview
@ExcludeFromJacocoGeneratedReport
fun DataSharingTermsDialogPreviewWithPrivateType() {
  AppTheme {
    DataSharingTermsDialog(
      dataSharingTerms =
        Survey.DataSharingTerms.newBuilder().setType(Survey.DataSharingTerms.Type.PRIVATE).build()
    )
  }
}

@Composable
@Preview
@ExcludeFromJacocoGeneratedReport
fun DataSharingTermsDialogPreviewWithPublicType() {
  AppTheme {
    DataSharingTermsDialog(
      dataSharingTerms =
        Survey.DataSharingTerms.newBuilder()
          .setType(Survey.DataSharingTerms.Type.PUBLIC_CC0)
          .build()
    )
  }
}

@Composable
@Preview
@ExcludeFromJacocoGeneratedReport
fun DataSharingTermsDialogPreviewWithCustomType() {
  AppTheme {
    DataSharingTermsDialog(
      dataSharingTerms =
        Survey.DataSharingTerms.newBuilder()
          .setType(Survey.DataSharingTerms.Type.CUSTOM)
          .setCustomText("Custom text")
          .build()
    )
  }
}

@Composable
@Preview
@ExcludeFromJacocoGeneratedReport
fun DataSharingTermsDialogPreviewWithNoType() {
  AppTheme {
    DataSharingTermsDialog(dataSharingTerms = Survey.DataSharingTerms.getDefaultInstance())
  }
}
