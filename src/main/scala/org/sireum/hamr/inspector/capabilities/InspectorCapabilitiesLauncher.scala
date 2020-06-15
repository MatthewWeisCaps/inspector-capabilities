/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.hamr.inspector.capabilities

import java.util.ServiceLoader

import art.{Art, ArtDebug, DataContent}
import org.sireum.hamr.inspector.common.InspectionBlueprint

object InspectorCapabilitiesLauncher {

  val THREAD_GROUP_NAME: String = "Art"

  private[capabilities] val instance: ProjectListener = load()
  private[capabilities] var serializer: DataContent => String = null
  private[capabilities] var deserializer: String => DataContent = null

  private final def load(): ProjectListener = {
    val providerIterator = ServiceLoader.load[ProjectListener](classOf[ProjectListener]).iterator()
    var service: ProjectListener = null

    while (providerIterator.hasNext) {
      if (service == null) {
        service = providerIterator.next()
      } else {
        println("Inspector Capabilities: ERROR Multiple services for ProjectListener detected. Will print them out then exit:")
        ServiceLoader.load[ProjectListener](classOf[ProjectListener]).iterator()
          .forEachRemaining(s => println(s"  found: ${s.getClass.toString}"))
        throw new IllegalStateException("Inspector Capabilities discovered multiple service implementations for ProjectListener")
      }
    }

    if (service == null) {
      println("Inspector Capabilities: No service provider found for ProjectListener. No inspection will occur.")
    }

    return service
  }

  // serializer returns org.sireum.String but deserializer takes normal String to prevent callers from needing
  // extra toString() calls when using JSON/MsgPack to create lambdas
  def run(blueprint: InspectionBlueprint): Unit = {
    if (instance != null) {
      val artThreadGroup = new ThreadGroup(Thread.currentThread.getThreadGroup, THREAD_GROUP_NAME)
      this.serializer = blueprint.serializer()
      this.deserializer = blueprint.deserializer()
      val thread = new Thread(artThreadGroup, () => {
        ArtDebug.registerListener(instance)
        Art.run(blueprint.ad())
      })
      thread.start()
    } else {
      // if no service provider was found, run art as normal (a warning message would have been printed during load())
      Art.run(blueprint.ad())
    }
  }

}
