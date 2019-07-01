/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.matrix.android.internal.session.notification

import arrow.core.Try
import im.vector.matrix.android.api.auth.data.SessionParams
import im.vector.matrix.android.api.pushrules.rest.PushRule
import im.vector.matrix.android.api.session.events.model.Event
import im.vector.matrix.android.api.session.room.RoomService
import im.vector.matrix.android.internal.session.pushers.DefaultConditionResolver
import im.vector.matrix.android.internal.task.Task
import timber.log.Timber
import javax.inject.Inject

internal interface ProcessEventForPushTask : Task<ProcessEventForPushTask.Params, Unit> {
    data class Params(
            val events: List<Event>,
            val rules: List<PushRule>
    )
}

internal class DefaultProcessEventForPushTask @Inject constructor(
        private val defaultPushRuleService: DefaultPushRuleService,
        private val roomService: RoomService,
        private val sessionParams: SessionParams
) : ProcessEventForPushTask {


    override suspend fun execute(params: ProcessEventForPushTask.Params): Try<Unit> {
        return Try {
            params.events.forEach { event ->
                fulfilledBingRule(event, params.rules)?.let {
                    Timber.v("Rule $it match for event ${event.eventId}")
                    defaultPushRuleService.dispatchBing(event, it)
                }
            }
            defaultPushRuleService.dispatchFinish()
        }
    }

    private fun fulfilledBingRule(event: Event, rules: List<PushRule>): PushRule? {
        val conditionResolver = DefaultConditionResolver(event, roomService, sessionParams)
        rules.filter { it.enabled }.forEach { rule ->
            val isFullfilled = rule.conditions?.map {
                it.asExecutableCondition()?.isSatisfied(conditionResolver) ?: false
            }?.fold(true/*A rule with no conditions always matches*/, { acc, next ->
                //All conditions must hold true for an event in order to apply the action for the event.
                acc && next
            }) ?: false

            if (isFullfilled) {
                return rule
            }
        }
        return null
    }

}