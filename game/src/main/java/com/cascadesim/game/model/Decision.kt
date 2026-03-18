package com.cascadesim.game.model

// Re-export models from common module for backward compatibility
@Deprecated("Use com.cascadesim.common.model.Decision instead", ReplaceWith("Decision", "com.cascadesim.common.model.Decision"))
typealias Decision = com.cascadesim.common.model.Decision

@Deprecated("Use com.cascadesim.common.model.DecisionType instead", ReplaceWith("DecisionType", "com.cascadesim.common.model.DecisionType"))
typealias DecisionType = com.cascadesim.common.model.DecisionType

@Deprecated("Use com.cascadesim.common.model.Event instead", ReplaceWith("Event", "com.cascadesim.common.model.Event"))
typealias Event = com.cascadesim.common.model.Event

@Deprecated("Use com.cascadesim.common.model.EventSeverity instead", ReplaceWith("EventSeverity", "com.cascadesim.common.model.EventSeverity"))
typealias EventSeverity = com.cascadesim.common.model.EventSeverity

@Deprecated("Use com.cascadesim.common.model.WorldState instead", ReplaceWith("WorldState", "com.cascadesim.common.model.WorldState"))
typealias WorldState = com.cascadesim.common.model.WorldState

@Deprecated("Use com.cascadesim.common.model.EventChain instead", ReplaceWith("EventChain", "com.cascadesim.common.model.EventChain"))
typealias EventChain = com.cascadesim.common.model.EventChain

@Deprecated("Use com.cascadesim.common.model.UiEventNode instead", ReplaceWith("UiEventNode", "com.cascadesim.common.model.UiEventNode"))
typealias UiEventNode = com.cascadesim.common.model.UiEventNode
