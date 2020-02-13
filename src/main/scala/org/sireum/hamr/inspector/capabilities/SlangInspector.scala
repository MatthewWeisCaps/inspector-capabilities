package org.sireum.hamr.inspector.capabilities

import java.util.ServiceLoader

import art.{ArchitectureDescription, Art, ArtDebug, DataContent}

object SlangInspector {

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
  def launchSlangProject(architectureDescription: ArchitectureDescription,
                         serializer: DataContent => org.sireum.String, deserializer: String => DataContent): Unit = {
    if (instance != null) {
      val artThreadGroup = new ThreadGroup(Thread.currentThread.getThreadGroup, THREAD_GROUP_NAME)
      this.serializer = serializer.andThen(_.toString())
      this.deserializer = deserializer // ".compose(_.toString())" would be necessary if deserializer took sireum String
      val thread = new Thread(artThreadGroup, () => {
        ArtDebug.registerListener(instance)
        Art.run(architectureDescription)
      })
      thread.start()
    } else {
      // if no service provider was found, run art as normal (a warning message would have been printed during load())
      Art.run(architectureDescription)
    }
  }

}
