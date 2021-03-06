package dev.challenge.minify.cache.aerospike

import com.aerospike.client.AerospikeClient
import com.aerospike.client.async.EventLoops

case class AerospikeClientWithEventLoops(client: AerospikeClient, eventLoops: EventLoops)
