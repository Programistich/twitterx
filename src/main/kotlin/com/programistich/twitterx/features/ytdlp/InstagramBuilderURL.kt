package com.programistich.twitterx.features.ytdlp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class InstagramBuilderURL(
    private val httpClient: HttpClient
) {
    suspend fun downloadIG(url: String): String {
        return runCatching {
            val igVideoResponse = httpClient.get {
                url(C.dc("l5VKR[9`b1E)N.yplMn<5+]{*T80x.PiWu9aU<B1m\$c%pE"))
                header("Referer", C.dc("l5VKR[9`aI~uHy7plMn<Q[[j<R|\$qx!Q"))
                header("Origin", C.dc("l5VKR[9`aI~uHy7plMn<Q[[j<R|\$qxO"))
                header("url", encodeUrl(url))
            }.body<IGVideoResponse>()

            igVideoResponse.video.firstOrNull()?.video ?: url
        }.getOrNull() ?: url
    }

    private fun encodeUrl(text: String): String {
        val keyBytes = C.dc("{%_1[[.ey#L)vmml<UAJ").toByteArray()
        val textBytes = text.toByteArray()

        val paddingSize = 16 - (textBytes.size % 16)
        val paddedBytes = ByteArray(textBytes.size + paddingSize)
        System.arraycopy(textBytes, 0, paddedBytes, 0, textBytes.size)
        for (i in textBytes.size until paddedBytes.size) {
            paddedBytes[i] = paddingSize.toByte()
        }
        return Cipher.getInstance("AES/ECB/PKCS5Padding")
            .apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES")) }
            .doFinal(paddedBytes)
            .let(Hex::encodeHexString)
    }
}

@Serializable
class IGVideoResponse(
    @SerialName("video")
    val video: List<IGVideo>
)

@Serializable
class IGVideo(
    @SerialName("video")
    val video: String
)

object C {
    private const val ab =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\""

    fun dc(i: String): String {
        var o = ""

        val aI = mutableMapOf<Char, Int>()
        ab.forEachIndexed { x, it -> aI[it] = x }

        if (i != "") {
            val l = i.length
            var b = 0
            var s = 0
            var v = -1

            for (ix in 0..<l) {
                val nV = aI[i[ix]]
                if (v < 0) {
                    v = nV!!
                } else {
                    v += (nV!! * 91)
                    b = b or (v shl s)

                    s += if (v and 8191 > 88) 13 else 14

                    do {
                        o += ((b and 255).toChar())
                        b = b shr 8
                        s -= 8
                    } while (s > 7)
                    v = -1
                }
            }
            if (v + 1 > 0) {
                val a = b or (v shl s)

                o += ((a and 255).toChar())
            }
        }

        return o
    }
}
