package net.osmand.telegram.utils

import android.os.AsyncTask
import net.osmand.PlatformUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


object AndroidNetworkUtils {

	private val log = PlatformUtil.getLog(AndroidNetworkUtils::class.java)

	interface OnRequestResultListener {
		fun onResult(result: String)
	}

	fun sendRequestAsync(urlText: String, listener: OnRequestResultListener?) {
		SendRequestTask(urlText, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
	}

	fun sendCreateNewDeviceRequestAsync(urlText: String, body: String, listener: OnRequestResultListener?) {
		SendCreateNewDeviceRequestTask(urlText, body, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
	}

	private class SendRequestTask(
		private val urlText: String,
		private val listener: OnRequestResultListener?
	) : AsyncTask<Void, Void, String?>() {

		override fun doInBackground(vararg params: Void): String? {
			return try {
				sendRequest(urlText)
			} catch (e: Exception) {
				log.error(e.message, e)
				null
			}
		}

		override fun onPostExecute(response: String?) {
			if (response != null) {
				listener?.onResult(response)
			}
		}
	}

	private class SendCreateNewDeviceRequestTask(
		private val urlText: String,
		private val body : String,
		private val listener: OnRequestResultListener?
	) : AsyncTask<Void, Void, String?>() {

		override fun doInBackground(vararg params: Void): String? {
			return try {
				createNewDeviceRequest(urlText, body)
			} catch (e: Exception) {
				log.error(e.message, e)
				null
			}
		}

		override fun onPostExecute(response: String?) {
			if (response != null) {
				listener?.onResult(response)
			}
		}
	}

	fun sendRequest(urlText: String): String? {
		try {
			log.info("GET : $urlText")
			val conn = getHttpURLConnection(urlText)
			conn.doInput = true
			conn.doOutput = false
			conn.requestMethod = "GET"
			conn.setRequestProperty("User-Agent", "OsmAnd Sharing")
			log.info("Response code and message : " + conn.responseCode + " " + conn.responseMessage)
			if (conn.responseCode != 200) {
				return conn.responseMessage
			}
			val inputStream = conn.inputStream
			val responseBody = StringBuilder()
			responseBody.setLength(0)
			if (inputStream != null) {
				val bufferedInput = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
				var s = bufferedInput.readLine()
				var first = true
				while (s != null) {
					if (first) {
						first = false
					} else {
						responseBody.append("\n")
					}
					responseBody.append(s)
					s = bufferedInput.readLine()
				}
				inputStream.close()
			}
			return responseBody.toString()
		} catch (e: IOException) {
			log.error(e.message, e)
			return e.message
		}
	}

	fun createNewDeviceRequest(url : String, body : String) : String? {
		var conn : HttpURLConnection? = null
		var result : String? = null
		try {
			conn = getHttpURLConnection(url)
			conn.doInput = true
			conn.doOutput = true
			conn.requestMethod = "POST"
			conn.setRequestProperty("User-Agent", "OsmAnd Sharing")
			conn.setRequestProperty("Accept", "application/json")
			conn.setRequestProperty("Content-Type", "application/json")
			conn.setRequestProperty(
				"Content-Length", body.toByteArray(charset("UTF-8")).size.toString())
			conn.useCaches = false
			conn.connectTimeout = 10 * 1000
			conn.readTimeout = 10 * 1000

			conn.setFixedLengthStreamingMode(body.toByteArray(charset("UTF-8")).size)


			val output = conn.outputStream

			output.use {
				output.write(body.toByteArray(charset("UTF-8")))
				output.flush()
			}

			val input = conn.inputStream

			val size = conn.getHeaderField("Content-Length").toInt()
			val buffer = ByteArray(size = size)
			input.use {
				input.read(buffer)
			}
			result = String(buffer, charset("UTF-8"))
		} catch (ex: IOException) {
			log.error(ex.message, ex)
			result = null
		} finally {
			conn?.disconnect()
		}
		return result;
	}

	@Throws(MalformedURLException::class, IOException::class)
	fun getHttpURLConnection(urlString: String): HttpURLConnection {
		return getHttpURLConnection(URL(urlString))
	}

	@Throws(IOException::class)
	fun getHttpURLConnection(url: URL): HttpURLConnection {
		return url.openConnection() as HttpURLConnection
	}
}