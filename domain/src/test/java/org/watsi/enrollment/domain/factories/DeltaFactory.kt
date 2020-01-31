package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.Delta
import java.util.UUID

object DeltaFactory {

    fun build(id: Int = 0,
              action: Delta.Action = Delta.Action.ADD,
              modelName: Delta.ModelName = Delta.ModelName.HOUSEHOLD,
              synced: Boolean = false,
              modelId: UUID = UUID.randomUUID(),
              field: String? = null
    ): Delta {
        return Delta(id = id,
                     action = action,
                     modelName = modelName,
                     modelId = modelId,
                     field = field,
                     synced = synced)
    }
}
