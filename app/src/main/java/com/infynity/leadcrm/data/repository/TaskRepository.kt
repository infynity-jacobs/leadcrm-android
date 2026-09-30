package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.TaskListResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import com.infynity.leadcrm.core.network.models.TaskUpdateRequest

class TaskRepository(
    private val api: LeadCrmApi
) {
    suspend fun getTasks(leadId: Int? = null): TaskListResponse {
        return api.getTasks(leadId = leadId)
    }

    suspend fun getTask(taskId: Int): TaskResponse {
        return api.getTask(taskId)
    }

    suspend fun updateTask(
        taskId: Int,
        request: TaskUpdateRequest
    ): TaskResponse {
        return api.updateTask(taskId, request)
    }

    suspend fun getTaskStatuses(): List<TaskStatusResponse> {
        return api.getTaskStatuses()
    }
}
