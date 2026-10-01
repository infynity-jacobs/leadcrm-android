package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.TaskListResponse
import com.infynity.leadcrm.core.network.models.TaskCreateRequest
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import com.infynity.leadcrm.core.network.models.TaskUpdateRequest
import com.infynity.leadcrm.core.network.models.TeamResponse
import com.infynity.leadcrm.core.network.UserResponse

class TaskRepository(
    private val api: LeadCrmApi
) {
    suspend fun getTasks(leadId: Int? = null): TaskListResponse {
        return api.getTasks(leadId = leadId)
    }

    suspend fun getTask(taskId: Int): TaskResponse {
        return api.getTask(taskId)
    }

    suspend fun createTask(request: TaskCreateRequest): TaskResponse {
        return api.createTask(request)
    }

    suspend fun updateTask(
        taskId: Int,
        request: TaskUpdateRequest
    ): TaskResponse {
        return api.updateTask(taskId, request)
    }

    suspend fun deleteTask(taskId: Int) {
        api.deleteTask(taskId)
    }

    suspend fun getTaskStatuses(): List<TaskStatusResponse> {
        return api.getTaskStatuses()
    }

    suspend fun getTeams(): List<TeamResponse> {
        return api.getTeams()
    }

    suspend fun getAssignees(teamId: Int): List<UserResponse> {
        return api.getUsers(
            role = "marketing_staff",
            teamId = teamId
        )
    }
}
