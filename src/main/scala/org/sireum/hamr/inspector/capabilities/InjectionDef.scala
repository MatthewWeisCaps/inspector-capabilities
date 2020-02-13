package org.sireum.hamr.inspector.capabilities

trait InjectionDef {

  def name: String
  def payload: Option[art.DataContent]
  def bridge: art.Bridge
  def port: art.UPort

  override def toString: String = getClass.getSimpleName

}
