package dev.challenge.minify.cache.aerospike

import com.aerospike.client.AerospikeClient
import com.aerospike.client.async.{EventPolicy, NioEventLoops}
import com.aerospike.client.policy.ClientPolicy

object Aerospike {

  def apply(config: AerospikeConfig): AerospikeClientWithEventLoops = {
    val eventPolicy = new EventPolicy
    val eventLoops = new NioEventLoops(eventPolicy, -1)

    val clientPolicy = new ClientPolicy()
    clientPolicy.eventLoops = eventLoops

    clientPolicy.writePolicyDefault.sendKey = true
    clientPolicy.queryPolicyDefault.sendKey = true

    clientPolicy.failIfNotConnected = false

    val client = new AerospikeClient(clientPolicy, config.host)

    AerospikeClientWithEventLoops(client, eventLoops)
  }

}
