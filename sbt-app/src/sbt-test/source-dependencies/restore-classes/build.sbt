import sbt.internal.inc.Analysis
import complete.DefaultParsers._

(Compile / crossTarget) := target.value

// Reset compiler iterations, necessary because tests run in batch mode
val recordPreviousIterations = taskKey[Unit]("Record previous iterations.")
recordPreviousIterations := {
  val log = streams.value.log
  CompileState.previousIterations = {
    val previousAnalysis = (Compile / previousCompile).value.analysis.asScala
    previousAnalysis match {
      case None =>
        log.info("No previous analysis detected")
        0
      case Some(a: Analysis) => a.compilations.allCompilations.size
      case Some(_) => -1 // should be unreachable but causes warnings
    }
  }
}

val checkIterations = inputKey[Unit]("Verifies the accumulated number of iterations of incremental compilation.")

checkIterations := {
  val expected: Int = (Space ~> NatBasic).parsed
  val actual: Int = ((Compile / compile).value match { case a: Analysis => a.compilations.allCompilations.size }) - CompileState.previousIterations
  assert(expected == actual, s"Expected $expected compilations, got $actual")
}
