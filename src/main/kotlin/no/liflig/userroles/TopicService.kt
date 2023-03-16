package no.liflig.userroles

/**
 * A generic service that represents a topic to send to
 */
interface TopicService {
  fun sendEvent(event: String)
}
