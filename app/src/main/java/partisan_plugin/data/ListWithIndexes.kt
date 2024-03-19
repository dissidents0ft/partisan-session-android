package partisan_plugin.data

import partisan_plugin.domain.entities.Indexed
import java.util.ArrayList

/**
 * List with index autoincrement
 */
class ListWithIndexes<T: Indexed> : ArrayList<T>() {
    private var index = 0

    override fun add(element: T): Boolean {
        if (element.index==0) {
            index++
            element.index = index
        }
        return super.add(element)
    }
}