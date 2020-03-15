package com.kos.kit.protobufio

import java.util.concurrent.TimeUnit

import okhttp3.{OkHttpClient, Request, RequestBody}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.concurrent.{ExecutionContext, Future}
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level

object OkClient {

	var enableLogging = false
	var loggingLevel = Level.BODY
	var readTimeout = 10 //Seconds
	var writeTimeout = 10 //Seconds
	var connectTimeout = 10 //Seconds

	def createClient(): OkHttpClient = {
		val clientBuilder = new OkHttpClient.Builder().
			readTimeout(readTimeout, TimeUnit.SECONDS)
			.writeTimeout(writeTimeout, TimeUnit.SECONDS)
			.connectTimeout(connectTimeout, TimeUnit.SECONDS)

		if (enableLogging) {
			val logging = new HttpLoggingInterceptor
			logging.setLevel(loggingLevel)
			clientBuilder.addInterceptor(logging)
		}

		clientBuilder.build()
	}

	def requestTo[A <: GeneratedMessage](uri: String,
										 data: GeneratedMessage,
										 gm: GeneratedMessageCompanion[A])(implicit client: OkHttpClient, executor: ExecutionContext): Future[A] = {
		Future(syncRequestTo(uri, data, gm))
	}

	def syncRequestTo[A <: GeneratedMessage](uri: String,
											 data: GeneratedMessage,
											 gm: GeneratedMessageCompanion[A])(implicit client: OkHttpClient): A = {

		val body = RequestBody.create(data.toByteArray)
		val rb = new Request.Builder()
		rb.url(uri).method("POST", body)


		val request = rb.build()
		val ex = client.newCall(request).execute()
		gm.parseFrom(ex.body().bytes())

	}


}
