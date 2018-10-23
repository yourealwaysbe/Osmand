package net.osmand.telegram.utils

import net.osmand.PlatformUtil
import net.osmand.telegram.TelegramApplication
import net.osmand.telegram.TelegramSettings
import org.json.JSONException
import org.json.JSONObject

object OsmandApiUtils {

	private val log = PlatformUtil.getLog(OsmandApiUtils::class.java)

	private val BASE_URL = "https://live.osmand.net"

	fun updateSharingDevices(app: TelegramApplication, userId: Int) {
		AndroidNetworkUtils.sendRequestAsync(
			"$BASE_URL/device/send-devices?uid=$userId",
			object : AndroidNetworkUtils.OnRequestResultListener {
				override fun onResult(result: String) {
					val list = parseJsonContents(result)
					app.settings.updateShareDevicesIds(list)
				}
			}
		)
	}

	fun createNewDevice(app : TelegramApplication, deviceName : String, chatId : Long,
						listener : AndroidNetworkUtils.OnRequestResultListener) {
		val user = app.telegramHelper.getCurrentUser()!!
		val body = JSONObject()
		body.put("deviceName", deviceName)
		// stub
		body.put("chatId", chatId)
		val jsonUser = JSONObject()
		jsonUser.put("id", user.id)
		jsonUser.put("firstName", user.firstName)
		// stub
		jsonUser.put("isBot", app.telegramHelper.isBot(user.id))
		jsonUser.put("lastName", user.lastName)
		jsonUser.put("userName", user.username)
		jsonUser.put("languageCode", user.languageCode)
		body.put("user", jsonUser)

		val jsonBody = body.toString(2)

		AndroidNetworkUtils.sendCreateNewDeviceRequestAsync(
			"$BASE_URL/device/new",
			jsonBody,
			listener
		)
	}

	fun parseDeviceBot(deviceJSON: JSONObject) : TelegramSettings.DeviceBot {
		val deviceBot = TelegramSettings.DeviceBot().apply {
			id = deviceJSON.getLong("id")
			userId = deviceJSON.getLong("userId")
			chatId = deviceJSON.getLong("chatId")
			deviceName = deviceJSON.getString("deviceName")
			externalId = deviceJSON.getString("externalId")
			data = deviceJSON.getString("data")
		}
		return deviceBot;
	}

	fun parseJsonContents(contentsJson: String): List<TelegramSettings.DeviceBot> {
		val list = mutableListOf<TelegramSettings.DeviceBot>()
		try {
			val jArray = JSONObject(contentsJson).getJSONArray("devices")
			for (i in 0 until jArray.length()) {
				val deviceJSON = jArray.getJSONObject(i)
				val deviceBot = parseDeviceBot(deviceJSON)
				list.add(deviceBot)
			}
		} catch (e: JSONException) {
			log.error(e.message, e)
		}
		return list
	}
}