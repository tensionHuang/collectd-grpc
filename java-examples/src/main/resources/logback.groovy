import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.INFO

def logLevel = INFO

appender("CONSOLE", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%d %contextName[%thread] %-5level %logger{5} - %m%n"
	}
}

root(logLevel, ["CONSOLE"])

