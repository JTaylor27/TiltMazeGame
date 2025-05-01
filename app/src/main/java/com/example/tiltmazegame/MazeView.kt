package com.example.tiltmazegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import kotlin.math.min
import java.util.Random

// --- Maze Generator ---
//AI Prompt - "What are some options for generating a maze in this project"
class MazeGenerator(val rows: Int, val cols: Int) {
    private val maze = Array(rows) { IntArray(cols) { 1 } }  // All walls

    private val directions = arrayOf(
        intArrayOf(-2, 0), intArrayOf(2, 0),  // Up, Down
        intArrayOf(0, -2), intArrayOf(0, 2)   // Left, Right
    )

    fun generateMaze(startRow: Int, startCol: Int) {
        for (i in maze.indices) {
            for (j in maze[i].indices) {
                maze[i][j] = 1
            }
        }

        val stack = mutableListOf<Pair<Int, Int>>()
        stack.add(Pair(startRow, startCol))
        maze[startRow][startCol] = 0

        while (stack.isNotEmpty()) {
            val (currentRow, currentCol) = stack.last()
            val neighbors = mutableListOf<Pair<Int, Int>>()

            for (dir in directions) {
                val newRow = currentRow + dir[0]
                val newCol = currentCol + dir[1]
                if (isValid(newRow, newCol) && maze[newRow][newCol] == 1) {
                    neighbors.add(Pair(newRow, newCol))
                }
            }

            if (neighbors.isNotEmpty()) {
                val (nextRow, nextCol) = neighbors[Random().nextInt(neighbors.size)]
                maze[nextRow][nextCol] = 0
                val midRow = (currentRow + nextRow) / 2
                val midCol = (currentCol + nextCol) / 2
                maze[midRow][midCol] = 0
                stack.add(Pair(nextRow, nextCol))
            } else {
                stack.removeAt(stack.size - 1)
            }
        }
    }

    private fun isValid(row: Int, col: Int): Boolean {
        return row in 0..<rows && col in 0..<cols
    }

    fun getMaze(): Array<IntArray> = maze
}

// --- Maze View ---
class MazeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val generator = MazeGenerator(11, 11)
    private var maze = Array(0) { IntArray(0) }

    private val wallPaint = Paint().apply { color = Color.BLACK }
    private val pathPaint = Paint().apply { color = Color.DKGRAY }
    private val goalPaint = Paint().apply { color = Color.GREEN }

    //AI Prompt - "What is a good way to implement a ball into the maze"
    private val ballDrawable = ContextCompat.getDrawable(context, R.drawable.ball_shape)

    private var tileSize: Int = 0
    private var offsetX = 0
    private var offsetY = 0

    private var ballX = 0f
    private var ballY = 0f
    private var ballRadius = 0f

    // --- Goal position ---
    private var goalRow = 0
    private var goalCol = 0

    private var startTime: Long = 0
    private var endTime: Long = 0
    private var timerStarted = false

    var listener: MazeListener? = null

    init {
        generator.generateMaze(1, 1)
        maze = generator.getMaze()

        // Find goal position: bottom-right most path tile
        for (row in maze.indices.reversed()) {
            for (col in maze[0].indices.reversed()) {
                if (maze[row][col] == 0) {
                    goalRow = row
                    goalCol = col
                    break
                }
            }
            if (goalRow != 0 && goalCol != 0) break
        }

        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (maze.isNotEmpty() && maze[0].isNotEmpty()) {
            tileSize = min(width / maze[0].size, height / maze.size)
            val totalMazeWidth = tileSize * maze[0].size
            val totalMazeHeight = tileSize * maze.size
            offsetX = (width - totalMazeWidth) / 2
            offsetY = (height - totalMazeHeight) / 2

            ballRadius = tileSize * 0.35f

            if (maze[1][1] == 0) {
                ballX = 1 * tileSize + tileSize / 2f
                ballY = 1 * tileSize + tileSize / 2f
            }
        }

        drawMaze(holder)

        startTime = System.currentTimeMillis()
        timerStarted = true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private fun drawMaze(holder: SurfaceHolder) {
        val canvas = holder.lockCanvas() ?: return

        canvas.drawColor(Color.LTGRAY)

        val rows = maze.size
        val cols = maze[0].size

        // Draw maze tiles
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val left = offsetX + col * tileSize
                val top = offsetY + row * tileSize
                val right = left + tileSize
                val bottom = top + tileSize

                val paint = if (maze[row][col] == 1) wallPaint else pathPaint
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }

        // --- Draw goal tile ---
        val goalCenterX = offsetX + goalCol * tileSize + tileSize / 2f
        val goalCenterY = offsetY + goalRow * tileSize + tileSize / 2f
        val goalRadius = tileSize * 0.3f
        canvas.drawCircle(goalCenterX, goalCenterY, goalRadius, goalPaint)

        // --- Draw ball ---
        val ballDrawX = offsetX + ballX
        val ballDrawY = offsetY + ballY

        val ballLeft = ballDrawX - ballRadius
        val ballTop = ballDrawY - ballRadius
        val ballRight = ballDrawX + ballRadius
        val ballBottom = ballDrawY + ballRadius

        ballDrawable?.setBounds(ballLeft.toInt(), ballTop.toInt(), ballRight.toInt(), ballBottom.toInt())
        ballDrawable?.draw(canvas)

        holder.unlockCanvasAndPost(canvas)
    }

    fun onTilt(dx: Float, dy: Float) {
        val speed = 1f

        val nextX = ballX + dx * speed
        val nextY = ballY + dy * speed

        val leftDistance = nextX - ballRadius
        val rightDistance = (maze[0].size - 1) * tileSize - nextX - ballRadius
        val leftCol = (leftDistance / tileSize).toInt()
        val rightCol = ((nextX + ballRadius) / tileSize).toInt()

        val canMoveX = leftDistance >= 0 && rightDistance >= 0 &&
                !isWall((ballY / tileSize).toInt(), leftCol) &&
                !isWall((ballY / tileSize).toInt(), rightCol)

        if (canMoveX) {
            ballX = nextX.coerceIn(ballRadius, (maze[0].size * tileSize) - ballRadius)
        }

        val topDistance = nextY - ballRadius
        val bottomDistance = (maze.size - 1) * tileSize - nextY - ballRadius
        val topRow = (topDistance / tileSize).toInt()
        val bottomRow = ((nextY + ballRadius) / tileSize).toInt()

        val canMoveY = topDistance >= 0 && bottomDistance >= 0 &&
                !isWall(topRow, (ballX / tileSize).toInt()) &&
                !isWall(bottomRow, (ballX / tileSize).toInt())

        if (canMoveY) {
            ballY = nextY.coerceIn(ballRadius, (maze.size * tileSize) - ballRadius)
        }

        drawMaze(holder)

        // --- Check if ball reached goal ---
        val ballRow = (ballY / tileSize).toInt()
        val ballCol = (ballX / tileSize).toInt()

        if (ballRow == goalRow && ballCol == goalCol) {
            onWin()
        }
    }

    private fun isWall(row: Int, col: Int): Boolean {
        return row in maze.indices && col in maze[0].indices && maze[row][col] == 1
    }

    // --- Win condition handler ---
    private fun onWin() {
        if (timerStarted) {
            endTime = System.currentTimeMillis()
            timerStarted = false
            val elapsedMillis = endTime - startTime
            listener?.onGameWin(elapsedMillis)
        }
    }


    interface MazeListener {
        fun onGameWin(elapsedMillis: Long)
    }

}

