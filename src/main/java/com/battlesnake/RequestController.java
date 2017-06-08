/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.battlesnake;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.battlesnake.data.HeadType;
import com.battlesnake.data.Move;
import com.battlesnake.data.MoveRequest;
import com.battlesnake.data.MoveResponse;
import com.battlesnake.data.Snake;
import com.battlesnake.data.StartRequest;
import com.battlesnake.data.StartResponse;
import com.battlesnake.data.TailType;
import com.google.common.collect.Sets;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {

    @RequestMapping(value = "/start", method = RequestMethod.POST, produces = "application/json")
    public StartResponse start(@RequestBody StartRequest request) {
        return new StartResponse()
                .setName("The Snake formerly known as the Mongoose")
                .setColor("#4A412A")
                .setHeadUrl("https://s3-us-west-2.amazonaws.com/s3-xmatters-static/snek/mongoose1.png")
                .setHeadType(HeadType.SANDWORM)
                .setTailType(TailType.FATRATTLE)
                .setTaunt("Your mother was a hamster!");
    }

    @RequestMapping(value = "/move", method = RequestMethod.POST, produces = "application/json")
    public MoveResponse move(@RequestBody MoveRequest request) {
        Moves moves = getAvailable(request);
        Move[] movesArr = moves.available.toArray(new Move[0]);

        Move finalMove = null;
        if (moves.winning.size() > 0) {
//      return new MoveResponse()
//              .setMove(moves.winning.iterator().next())
//              .setTaunt("DIE !!!");
        }

        Set<Move> foodMoves = moveToFood(request);
        if (foodMoves.size() > 0) {
            Move[] foodMovesArr = foodMoves.toArray(new Move[0]);
            if (foodMoves.size() > 1) {
                finalMove = findBest(foodMoves, request);
            } else {
                finalMove = foodMoves.iterator().next();
            }
        } else {
            finalMove = movesArr[0];
        }

        return new MoveResponse()
                .setMove(finalMove)
                .setTaunt("Move");
    }

    private Move findBest(Set<Move> foodMoves, MoveRequest request) {
        Snake mySnake = getMySnake(request);

        int[] myCords = getMySnakeCords(request);
        int x = myCords[0];
        int y = myCords[1];

        Move[] foodMovesArr = foodMoves.toArray(new Move[0]);

        int rank = getRank(foodMovesArr[0], request, x, y);
        int rank2 = getRank(foodMovesArr[1], request, x, y);
        if (rank > rank2) {
            return foodMovesArr[0];
        } else {
            return foodMovesArr[1];
        }
    }

    private int getRank(Move foodMove, MoveRequest request, int x, int y) {
        int rank = 0;
        if (foodMove == Move.RIGHT) {
            int newX = x;
            for (int i = 0; i < request.getWidth(); i++) {
                newX++;
                if (isValid(newX, y, request)) {
                    rank++;
                } else {
                    break;
                }
            }
        } else if (foodMove == Move.LEFT) {
            int newX = x;
            for (int i = 0; i < request.getWidth(); i++) {
                newX--;
                if (isValid(newX, y, request)) {
                    rank++;
                } else {
                    break;
                }
            }
        } else if (foodMove == Move.DOWN) {
            int newY = y;
            for (int i = 0; i < request.getWidth(); i++) {
                newY++;
                if (isValid(x, newY, request)) {
                    rank++;
                } else {
                    break;
                }
            }
        } else if (foodMove == Move.UP) {
            int newY = y;
            for (int i = 0; i < request.getWidth(); i++) {
                newY--;
                if (isValid(x, newY, request)) {
                    rank++;
                } else {
                    break;
                }
            }
        }
        return rank;
    }

    boolean isValid(int x, int y, MoveRequest request) {
        if (x > request.getWidth() - 1) {   //against walls
            return false;
        }
        if (x < 0) {
            return false;
        }
        if (y > request.getHeight() - 1) {
            return false;
        }
        if (y < 0) {
            return false;
        }
        for (Snake snake : request.getSnakes()) {   //against snakes
            for (int i = 0; i < snake.getCoords().length; i++) {
                int[] cords = snake.getCoords()[i];
                if (cords[0] == x && cords[1] == y) {
                    return false;
                }
                if (cords[0] == x - 1 && cords[1] == y) {
                    return false;
                }
                if (cords[0] == x && cords[1] == y + 1) {
                    return false;
                }
                if (cords[0] == x && cords[1] == y - 1) {
                    return false;
                }
            }
        }
        return true;
    }

    @RequestMapping(value = "/end", method = RequestMethod.POST)
    public Object end() {
        // No response required
        Map<String, Object> responseObject = new HashMap<String, Object>();
        return responseObject;
    }


    private static class Moves {
        public Set<Move> available = Sets.newHashSet(Move.DOWN, Move.UP, Move.LEFT, Move.RIGHT);
        public Set<Move> winning = Sets.newHashSet();
    }

    private Moves getAvailable(MoveRequest request) {
        Moves moves = new Moves();
        Snake mySnake = getMySnake(request);
        int mySnakeLen = getSnakeLen(mySnake);

        int[] myCords = getMySnakeCords(request);
        int x = myCords[0];
        int y = myCords[1];
        if (x + 1 > request.getWidth() - 1) {   //against walls
            moves.available.remove(Move.RIGHT);
        }
        if (x - 1 < 0) {
            moves.available.remove(Move.LEFT);
        }
        if (y + 1 > request.getHeight() - 1) {
            moves.available.remove(Move.DOWN);
        }
        if (y - 1 < 0) {
            moves.available.remove(Move.UP);
        }
        for (Snake snake : request.getSnakes()) {   //against snakes
            for (int i = 0; i < snake.getCoords().length; i++) {
                int[] cords = snake.getCoords()[i];
                if (cords[0] == x + 1 && cords[1] == y) {
                    moves.available.remove(Move.RIGHT);
                }
                if (cords[0] == x - 1 && cords[1] == y) {
                    moves.available.remove(Move.LEFT);
                }
                if (cords[0] == x && cords[1] == y + 1) {
                    moves.available.remove(Move.DOWN);
                }
                if (cords[0] == x && cords[1] == y - 1) {
                    moves.available.remove(Move.UP);
                }
            }
        }


        return moves;
    }

    private int getSnakeLen(Snake snake) {
        return snake.getCoords().length;
    }

    private Snake getMySnake(MoveRequest request) {
        for (Snake snake : request.getSnakes()) {
            if (snake.getId().equals(request.getYou())) {
                return snake;
            }
        }
        return null;
    }

    private Snake getOponentSnake(MoveRequest request) {
        for (Snake snake : request.getSnakes()) {
            if (!snake.getId().equals(request.getYou())) {
                return snake;
            }
        }
        return null;
    }

    private int[] getMySnakeCords(MoveRequest request) {
        Snake snake = getMySnake(request);
        return new int[]{snake.getCoords()[0][0], snake.getCoords()[0][1]};
    }

    private Set<Move> moveToFood(MoveRequest request) {

        // no food
        if (request.getFood().length == 0) {
            return Collections.emptySet();
        }

        Set<Move> options = new HashSet<>(2);
        Snake me = getMySnake(request);

        Moves availableMoves = getAvailable(request);

        int[] food = request.getFood()[0];
        int[] head = me.getCoords()[0];

        Move xMove, yMove;

        int xDist = head[0] - food[0];
        int yDist = head[1] - food[1];

        // x move
        if (head[0] > food[0] && availableMoves.available.contains(Move.LEFT)) {
            options.add(Move.LEFT);
        } else if (head[0] < food[0] && availableMoves.available.contains(Move.RIGHT)) {
            options.add(Move.RIGHT);
        } // else no x move

        // y -move
        if (head[1] > food[1] && availableMoves.available.contains(Move.UP)) {
            options.add(Move.UP);
        } else if (head[1] < food[1] && availableMoves.available.contains(Move.DOWN)) {
            options.add(Move.DOWN);
        }
        return options;
    }
}

