package com.example.basketballhelp.data.network.service

import com.example.basketballhelp.data.network.dto.DrillChecklistItemDto
import com.example.basketballhelp.data.network.dto.DrillCompletionPayloadDto
import com.example.basketballhelp.data.network.dto.GoalDto
import com.example.basketballhelp.data.network.dto.GoalPayloadDto
import com.example.basketballhelp.data.network.dto.PlayerDto
import com.example.basketballhelp.data.network.dto.PlayerPayloadDto
import com.example.basketballhelp.data.network.dto.SessionDto
import com.example.basketballhelp.data.network.dto.SessionPayloadDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HoopDevApiService {
    @GET("api/sessions")
    suspend fun getSessions(
        @Query("playerId") playerId: Int,
        @Query("limit") limit: Int? = null,
    ): List<SessionDto>

    @POST("api/sessions")
    suspend fun createSession(@Body body: SessionPayloadDto): SessionDto

    @GET("api/sessions/{id}")
    suspend fun getSession(@Path("id") id: Int): SessionDto

    @PUT("api/sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Int,
        @Body body: SessionPayloadDto,
    ): SessionDto

    @DELETE("api/sessions/{id}")
    suspend fun deleteSession(@Path("id") id: Int)

    @GET("api/goals")
    suspend fun getGoals(@Query("playerId") playerId: Int): List<GoalDto>

    @POST("api/goals")
    suspend fun createGoal(@Body body: GoalPayloadDto): GoalDto

    @GET("api/drills")
    suspend fun getDrills(@Query("date") date: String): List<DrillChecklistItemDto>

    @POST("api/drills")
    suspend fun saveDrillCompletion(@Body body: DrillCompletionPayloadDto)

    @GET("api/players")
    suspend fun getPlayers(): List<PlayerDto>

    @POST("api/players")
    suspend fun createPlayer(@Body body: PlayerPayloadDto): PlayerDto

    @GET("api/players/{id}")
    suspend fun getPlayer(@Path("id") id: Int): PlayerDto

    @PUT("api/players/{id}")
    suspend fun updatePlayer(
        @Path("id") id: Int,
        @Body body: PlayerPayloadDto,
    ): PlayerDto
}
