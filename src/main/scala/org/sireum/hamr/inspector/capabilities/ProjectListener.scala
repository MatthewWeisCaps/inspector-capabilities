package org.sireum.hamr.inspector.capabilities

import art.ArtListener

protected[capabilities] trait ProjectListener extends ArtListener with BaseMutable {

  // All methods are handled in their respective implementation.
  // Implementations are automatically discovered when SlangInspector.launchSlangInspector() is called.

}
