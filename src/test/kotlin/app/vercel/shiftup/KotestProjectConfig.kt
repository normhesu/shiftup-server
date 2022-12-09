package app.vercel.shiftup

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode

@Suppress("unused")
object KotestProjectConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerLeaf
    override val globalAssertSoftly = true
    override val parallelism = 3
}
