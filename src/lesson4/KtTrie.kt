package lesson4

import java.util.*

/**
 * Префиксное дерево для строк
 */
class KtTrie : AbstractMutableSet<String>(), MutableSet<String> {

    private class Node {
        val children: SortedMap<Char, Node> = sortedMapOf()
    }

    private val root = Node()

    override var size: Int = 0
        private set

    override fun clear() {
        root.children.clear()
        size = 0
    }

    private fun String.withZero() = this + 0.toChar()

    private fun findNode(element: String): Node? {
        var current = root
        for (char in element) {
            current = current.children[char] ?: return null
        }
        return current
    }

    override fun contains(element: String): Boolean =
        findNode(element.withZero()) != null

    override fun add(element: String): Boolean {
        var current = root
        var modified = false
        for (char in element.withZero()) {
            val child = current.children[char]
            if (child != null) {
                current = child
            } else {
                modified = true
                val newChild = Node()
                current.children[char] = newChild
                current = newChild
            }
        }
        if (modified) {
            size++
        }
        return modified
    }

    override fun remove(element: String): Boolean {
        val current = findNode(element) ?: return false
        if (current.children.remove(0.toChar()) != null) {
            size--
            return true
        }
        return false
    }

    /**
     * Итератор для префиксного дерева
     *
     * Спецификация: [java.util.Iterator] (Ctrl+Click по Iterator)
     *
     * Сложная
     */
    override fun iterator(): MutableIterator<String> = KtTrieIterator()

    inner class KtTrieIterator : MutableIterator<String> {
        private var thisWord: String? = null
        private val stack = Stack<String>()

        init {
            fillStack(root, "")
        }

        private fun fillStack(thisNode: Node, word: String) {
            for (child in thisNode.children)
                if (child.key != 0.toChar()) fillStack(child.value, word + child.key)
                else stack.add(word)
        }

        //T = O(1)
        //R = O(1)
        override fun hasNext(): Boolean = !stack.isEmpty()

        //T = O(1)
        //R = O(1)
        override fun next(): String {
            if (hasNext()) {
                thisWord = stack.pop()
                return thisWord!!
            } else throw NoSuchElementException()
        }

        //T = O(N),
        //R = O(N),
        //где N - длина удаляемого слова
        override fun remove() {
            if (thisWord != null) {
                remove(thisWord)
                thisWord = null
            } else throw IllegalStateException()
        }
    }
}