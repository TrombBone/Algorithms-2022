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
        private var thisNode: Node = root
        private val stack = Stack<Pair<Int, Node>>()
        private var thisWord: String? = null

        init {
            stack.push(0 to thisNode)
        }

        private fun isLastDeleted(): Boolean {
            var keys = thisNode.children.keys
            if (keys.isEmpty()) return false
            var literal = keys.last()
            var thisNode = this.thisNode
            while (literal != 0.toChar()) {
                thisNode = thisNode.children[literal]!!
                keys = thisNode.children.keys
                if (keys.isEmpty()) return false
                literal = keys.last()
            }
            return true
        }

        //T = O(N), где N - длина последнего (в алфавитном порядке) слова в дереве
        //R = O(1)
        override fun hasNext(): Boolean = stack.first().first < root.children.size && isLastDeleted()

        //T = O(N), гду N - это длина слова
        //R = O(1)
        override fun next(): String {
            if (!hasNext()) throw NoSuchElementException()
            var word = ""

            fun goUp() {
                while (true) {
                    val thisPair = stack.pop()
                    thisNode = thisPair.second
                    if ((thisNode.children.size > 1 && thisPair.first + 1 < thisNode.children.size) || thisNode == root) {
                        stack.push(Pair(thisPair.first + 1, thisNode))
                        break
                    }
                }
            }

            while (true) {
                val keys = thisNode.children.keys.toList()
                if (keys.isEmpty()) {
                    goUp()
                    continue
                }
                val key = keys[stack.peek().first]
                if (key == 0.toChar()) {
                    stack.pop()
                    break
                }
                thisNode = thisNode.children[key]!!
                stack.push(0 to thisNode)
            }
            for ((literalIndex, node) in stack) word += node.children.keys.toList()[literalIndex]
            thisWord = word

            // go to intersection point next word
            val keys = thisNode.children.keys.toList()
            if (keys.size > 1) stack.push(1 to thisNode)
            else goUp()

            return word
        }

        //T = O(N),
        //R = O(N),
        //где N - длина удаляемого слова
        override fun remove() {
            if (thisWord != null) {
                if (thisNode.children.keys.toList()[0] == 0.toChar() && stack.peek().first == 1) {
                    val oldIndex = stack.pop().first
                    stack.push(Pair(oldIndex - 1, thisNode))
                }
                remove(thisWord)
                thisWord = null
            } else throw IllegalStateException()
        }
    }
}