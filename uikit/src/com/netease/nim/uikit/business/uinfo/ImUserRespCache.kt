package com.netease.nim.uikit.business.uinfo

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class ImUserRespDTO {

    var address = ""
    var avatar = ""
    var dataMasking = false
    var name = ""
    var phone = ""
    var registerDate = ""

    /** 1 小助手 */
    var memberLevel = 0
    var title = ""
    var userId = 0L

}

abstract class ImUserRespCache {

    protected constructor(context: Context, currentLoginUserId: Long) {
        sprefs =
            context.getSharedPreferences("im_user_resp_$currentLoginUserId", Context.MODE_PRIVATE)
    }

    interface Callback {
        fun onDone()
    }

    companion object {
        lateinit var instance: ImUserRespCache
        const val MAX_MEMORY_CACHE = 50
    }

    private var sprefs: SharedPreferences? = null
    private val cache = mutableMapOf<String, ImUserRespDTO>()
    private var editor: SharedPreferences.Editor? = null
    private val gson = Gson()

    fun get(sessionId: String): ImUserRespDTO? {
        var dto = cache[sessionId]
        if (dto == null) {
            val value = sprefs!!.getString(sessionId, null)
            if (value != null) {
                dto = gson.fromJson(value, ImUserRespDTO::class.java)
            }
        }
        return dto
    }

    fun edit() {
        if (editor == null)
            editor = sprefs!!.edit()
    }

    fun put(dtos: List<ImUserRespDTO>) {
        edit()
        dtos.forEach { put("${it.userId}", it) }
        commit()
    }

    fun put(userId: String, dto: ImUserRespDTO) {
        if (!cache.containsKey(userId)) {
            cache[userId] = dto
            if (cache.size > MAX_MEMORY_CACHE) {
                cache.remove(cache.keys.last())
            }
        }
        editor?.putString(userId, gson.toJson(dto))
    }

    fun commit() {
        editor?.commit()
        editor = null
    }

    abstract fun load(userIdList: List<String>, callback: Callback)
    abstract fun getCurrentUserAvatar(): String

    /**
     * 如果能跳转查看店铺详情，返回 true, 否则 false
     */
    abstract fun previewProfile(context: Context, sessionId: String): Boolean

}