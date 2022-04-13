package lesson3

import java.util.*
import kotlin.math.max

// attention: Comparable is supported but Comparator is not
class KtBinarySearchTree<T : Comparable<T>> : AbstractMutableSet<T>(), CheckableSortedSet<T> {

    private class Node<T>(
        val value: T
    ) {
        var left: Node<T>? = null
        var right: Node<T>? = null
    }

    private var root: Node<T>? = null

    override var size = 0
        private set

    private fun find(value: T): Node<T>? =
        root?.let { find(it, value) }

    private fun find(start: Node<T>, value: T): Node<T> {
        val comparison = value.compareTo(start.value)
        return when {
            comparison == 0 -> start
            comparison < 0 -> start.left?.let { find(it, value) } ?: start
            else -> start.right?.let { find(it, value) } ?: start
        }
    }

    /**
     * Возвращает родителя искомого элемента
     * Для root вернёт root
     *
     * T = O(h)
     * R = O(1)
     */
    private fun findWithParent(start: Node<T> = root!!, parent: Node<T> = start, value: T): Node<T> {
        val comparison = value.compareTo(start.value)
        return when {
            comparison == 0 -> parent
            comparison < 0 -> start.left?.let { findWithParent(it, start, value) } ?: parent
            else -> start.right?.let { findWithParent(it, start, value) } ?: parent
        }
    }

    override operator fun contains(element: T): Boolean {
        val closest = find(element)
        return closest != null && element.compareTo(closest.value) == 0
    }

    /**
     * Добавление элемента в дерево
     *
     * Если элемента нет в множестве, функция добавляет его в дерево и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     *
     * Спецификация: [java.util.Set.add] (Ctrl+Click по add)
     *
     * Пример
     */
    override fun add(element: T): Boolean {
        val closest = find(element)
        val comparison = if (closest == null) -1 else element.compareTo(closest.value)
        if (comparison == 0) {
            return false
        }
        val newNode = Node(element)
        when {
            closest == null -> root = newNode
            comparison < 0 -> {
                assert(closest.left == null)
                closest.left = newNode
            }
            else -> {
                assert(closest.right == null)
                closest.right = newNode
            }
        }
        size++
        return true
    }

    /**
     * Удаление элемента из дерева
     *
     * Если элемент есть в множестве, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     *
     * Спецификация: [java.util.Set.remove] (Ctrl+Click по remove)
     * (в Котлине тип параметера изменён с Object на тип хранимых в дереве данных)
     *
     * Средняя
     *
     * T = O(h), где h - высота дерева
     * R = O(1)
     */
    override fun remove(element: T): Boolean {
        if (!contains(element)) return false

        val nodeToDel = find(element)!!
        val newNode: Node<T>? =
            if (nodeToDel.right != null) findMinNode(nodeToDel.right!!)
            else if (nodeToDel.left != null) nodeToDel.left
            else null

        val parentNewNode: Node<T>? = if (newNode != null) findWithParent(value = newNode.value) else null
        val isLeftChildNew = if (parentNewNode != null) parentNewNode.left == newNode else null
        if (parentNewNode != null)
            if (isLeftChildNew!!)
                parentNewNode.left = if (newNode!!.right != null) newNode.right else null
            else
                parentNewNode.right = if (newNode!!.right != null) newNode.right else null

        val parentDelNode: Node<T>? = if (nodeToDel != root) findWithParent(value = element) else null
        val isLeftChildDel = if (parentDelNode != null) parentDelNode.left == nodeToDel else null
        if (parentDelNode != null) if (isLeftChildDel!!) parentDelNode.left = newNode else parentDelNode.right = newNode

        if (newNode != null) {
            if (parentNewNode == nodeToDel) {
                if (isLeftChildNew!!) {
                    if (newNode.right == null) {
                        newNode.right = nodeToDel.right
                    }
                } else newNode.left = nodeToDel.left
            } else {
                newNode.left = nodeToDel.left
                newNode.right = nodeToDel.right
            }
        }

        if (nodeToDel == root) root = newNode
        size--

        return true
    }

    //T = O(N), где N - это количество левых потомков
    //R = O(1)
    private fun findMinNode(startNode: Node<T>): Node<T> =
        if (startNode.left == null) startNode else findMinNode(startNode.left!!)

    override fun comparator(): Comparator<in T>? =
        null

    override fun iterator(): MutableIterator<T> =
        BinarySearchTreeIterator()

    inner class BinarySearchTreeIterator internal constructor() : MutableIterator<T> {
        private var index = -1
        private lateinit var thisNode: Node<T>
        private lateinit var prevNode: Node<T>
        private var isRemoveWasCall = false
        private val stack = Stack<Node<T>>()

        init {
            fillStack()
        }

        private fun fillStack() {
            var thisNode = root
            if (thisNode != null) {
                stack.push(thisNode)
                while (thisNode!!.left != null) {
                    stack.push(thisNode.left)
                    thisNode = thisNode.left
                }
            }
        }

        //T = O(N), где N - это количество левых потомков
        //R = O(1)
        private fun findMinNodeWithStack(startNode: Node<T>): Node<T> {
            stack.push(startNode)
            return if (startNode.left == null) startNode else findMinNodeWithStack(startNode.left!!)
        }

        /**
         * Проверка наличия следующего элемента
         *
         * Функция возвращает true, если итерация по множеству ещё не окончена (то есть, если вызов next() вернёт
         * следующий элемент множества, а не бросит исключение); иначе возвращает false.
         *
         * Спецификация: [java.util.Iterator.hasNext] (Ctrl+Click по hasNext)
         *
         * Средняя
         *
         * T = O(1)
         * R = O(1)
         */
        override fun hasNext(): Boolean = index < size - 1

        /**
         * Получение следующего элемента
         *
         * Функция возвращает следующий элемент множества.
         * Так как BinarySearchTree реализует интерфейс SortedSet, последовательные
         * вызовы next() должны возвращать элементы в порядке возрастания.
         *
         * Бросает NoSuchElementException, если все элементы уже были возвращены.
         *
         * Спецификация: [java.util.Iterator.next] (Ctrl+Click по next)
         *
         * Средняя
         *
         * T = O(1)
         * R = O(1)
         */
        override fun next(): T {
            if (index == size - 1) throw NoSuchElementException()
            index++
            isRemoveWasCall = false
            //first call
            if (index == 0) {
                thisNode = stack.last()
                prevNode = thisNode
                return stack.pop().value
            }
            //go to right
            if (thisNode.right != null) {
                prevNode = thisNode
                thisNode = findMinNodeWithStack(thisNode.right!!)
                return stack.pop().value
            }
            //if thisNode is last element
            if (stack.isEmpty()) throw NoSuchElementException()
            //go to parent if parent.left == thisNode
            prevNode = thisNode
            thisNode = stack.pop()
            return thisNode.value
        }

        /**
         * Удаление предыдущего элемента
         *
         * Функция удаляет из множества элемент, возвращённый крайним вызовом функции next().
         *
         * Бросает IllegalStateException, если функция была вызвана до первого вызова next() или же была вызвана
         * более одного раза после любого вызова next().
         *
         * Спецификация: [java.util.Iterator.remove] (Ctrl+Click по remove)
         *
         * Сложная
         *
         * T = O(log(N))
         * R = O(1)
         */
        override fun remove() {
            if (index < 0 || isRemoveWasCall) throw IllegalStateException()
            isRemoveWasCall = true
            thisNode.right?.let { stack.push(findMinNodeWithStack(it)) }
            remove(thisNode.value)
            if (prevNode != thisNode) thisNode = prevNode
            index--
        }

    }

    /**
     * Подмножество всех элементов в диапазоне [fromElement, toElement)
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева, которые
     * больше или равны fromElement и строго меньше toElement.
     * При равенстве fromElement и toElement возвращается пустое множество.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.subSet] (Ctrl+Click по subSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Очень сложная (в том случае, если спецификация реализуется в полном объёме)
     */
    override fun subSet(fromElement: T, toElement: T): SortedSet<T> {
        TODO()
    }

    /**
     * Подмножество всех элементов строго меньше заданного
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева строго меньше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.headSet] (Ctrl+Click по headSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Сложная
     */
    override fun headSet(toElement: T): SortedSet<T> {
        TODO()
    }

    /**
     * Подмножество всех элементов нестрого больше заданного
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева нестрого больше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.tailSet] (Ctrl+Click по tailSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Сложная
     */
    override fun tailSet(fromElement: T): SortedSet<T> {
        TODO()
    }

    override fun first(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.left != null) {
            current = current.left!!
        }
        return current.value
    }

    override fun last(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.right != null) {
            current = current.right!!
        }
        return current.value
    }

    override fun height(): Int =
        height(root)

    private fun height(node: Node<T>?): Int {
        if (node == null) return 0
        return 1 + max(height(node.left), height(node.right))
    }

    override fun checkInvariant(): Boolean =
        root?.let { checkInvariant(it) } ?: true

    private fun checkInvariant(node: Node<T>): Boolean {
        val left = node.left
        if (left != null && (left.value >= node.value || !checkInvariant(left))) return false
        val right = node.right
        return right == null || right.value > node.value && checkInvariant(right)
    }

}