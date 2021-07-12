package waybilmobile.company.waybilbuyer.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomeNestedScrollView(context: Context, attrs: AttributeSet) : NestedScrollView(context, attrs) {

    override fun  onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        val rv = target as RecyclerView
        if (dy < 0 && isRvScrolledToTop(rv) || dy > 0 && !isNsvScrolledToBottom(
                this
            )
        ) {
            scrollBy(0, dy)
            consumed[1] = dy
            return
        }
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedPreFling(target: View, velX: Float, velY: Float): Boolean {
        val rv = target as RecyclerView
        if (velY < 0 && isRvScrolledToTop(rv) || velY > 0 && !isNsvScrolledToBottom(
                this
            )
        ) {
            fling(velY.toInt())
            return true
        }
        return super.onNestedPreFling(target, velX, velY)
    }

    companion object {
        private fun isNsvScrolledToBottom(nsv: NestedScrollView): Boolean {
            return !nsv.canScrollVertically(1)
        }

        private fun isRvScrolledToTop(rv: RecyclerView): Boolean {
            val lm = rv.layoutManager as LinearLayoutManager?
            return (lm!!.findFirstVisibleItemPosition() == 0
                    && lm.findViewByPosition(0)!!.top == 0)
        }
    }
}