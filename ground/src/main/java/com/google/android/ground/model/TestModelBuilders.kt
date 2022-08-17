/*
 * Copyright 2021 Google LLC
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
package com.google.android.ground.model

import com.google.android.ground.model.locationofinterest.Point
import com.google.android.ground.model.locationofinterest.PointOfInterest
import com.google.android.ground.model.locationofinterest.PointOfInterest.Companion.newBuilder
import com.google.android.ground.model.task.Task
import com.google.common.collect.ImmutableList
import com.google.firebase.firestore.GeoPoint
import java.util.*

/**
 * Helper methods for building model objects for use in tests. Method return builders with required
 * fields set to placeholder values. Rather than depending on these values, tests that test for
 * specific values should explicitly set them in relevant test methods or during test setup.
 */
object TestModelBuilders {
    @JvmStatic
    fun newSurvey(): Survey.Builder =
        Survey.newBuilder().setId("").setTitle("").setDescription("")

    @JvmStatic
    fun newAuditInfo(): AuditInfo.Builder =
        AuditInfo.builder().setClientTimestamp(Date(0)).setUser(
            User("", "", "")
        )

    private fun newPoint(): Point.Builder =
        Point.newBuilder().setLatitude(0.0).setLongitude(0.0)

    private fun newGeoPoint(): GeoPoint =
        GeoPoint(0.0, 0.0)

    @JvmStatic
    fun newGeoPointPolygonVertices(): ImmutableList<GeoPoint> {
        return ImmutableList.builder<GeoPoint>()
            .add(newGeoPoint())
            .add(newGeoPoint())
            .add(newGeoPoint())
            .build()
    }

    @JvmStatic
    fun newPointOfInterest(): PointOfInterest.Builder {
        return newBuilder()
            .setId("")
            .setSurvey(newSurvey().build())
            .setPoint(newPoint().build())
            .setCreated(newAuditInfo().build())
            .setLastModified(newAuditInfo().build())
    }

    @JvmStatic
    fun newTermsOfService(): TermsOfService.Builder =
        TermsOfService.builder().setId("").setText("")

    @JvmStatic
    fun newTask(): Task.Builder {
        return Task.newBuilder()
            .setId("")
            .setIndex(0)
            .setType(Task.Type.TEXT_FIELD)
            .setLabel("")
            .setRequired(false)
    }
}