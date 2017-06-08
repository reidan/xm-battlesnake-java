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

import com.battlesnake.data.*;

import com.google.common.collect.Sets;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@RestController
public class RequestController {

  @RequestMapping(value="/start", method=RequestMethod.POST, produces="application/json")
  public StartResponse start(@RequestBody StartRequest request) {
    return new StartResponse()
      .setName("Bowser Snake")
      .setColor("#FF0000")
      .setHeadUrl("http://vignette1.wikia.nocookie.net/nintendo/images/6/61/Bowser_Icon.png/revision/latest?cb=20120820000805&path-prefix=en")
      .setHeadType(HeadType.DEAD)
      .setTailType(TailType.PIXEL)
      .setTaunt("Roarrrrrrrrr!");
  }

  @RequestMapping(value="/move", method=RequestMethod.POST, produces = "application/json")
  public MoveResponse move(@RequestBody MoveRequest request) {
    Set<Move> availableMoveOptions = getAvailable(request).available;
    Move[] movesArr = availableMoveOptions.toArray(new Move[0]);

    return new MoveResponse()
      .setMove(movesArr[new Random().nextInt(movesArr.length)])
      .setTaunt("Going Up!");
  }

  @RequestMapping(value="/end", method=RequestMethod.POST)
  public Object end() {
      // No response required
      Map<String, Object> responseObject = new HashMap<String, Object>();
      return responseObject;
  }


  private static class Moves{
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
          if (i == 0 && snake != mySnake && getSnakeLen(snake) < mySnakeLen) {
            moves.winning.add(Move.RIGHT);
          } else {
            moves.available.remove(Move.RIGHT);
          }
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
      if(snake.getId().equals(request.getYou())) {
        return snake;
      }
    }
    return null;
  }

  private Snake getOponentSnake(MoveRequest request) {
    for (Snake snake : request.getSnakes()) {
      if(!snake.getId().equals(request.getYou())) {
        return snake;
      }
    }
    return null;
  }

  private int[] getMySnakeCords(MoveRequest request) {
    Snake snake = getMySnake(request);
    return new int[]{snake.getCoords()[0][0],snake.getCoords()[0][1]};
  }
}
