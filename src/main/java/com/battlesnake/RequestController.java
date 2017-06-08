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
    Set<Move> availableMoveOptions = getAvailable(request);
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


  private Set<Move> getAvailable(MoveRequest request) {
    HashSet<Move> moves = Sets.newHashSet(Move.DOWN, Move.UP, Move.LEFT, Move.RIGHT);
    int[] myCords = getMySnakeCords(request);
    int x = myCords[0];
    int y = myCords[0];
    if (x + 1 > request.getWidth() - 1) {
      moves.remove(Move.RIGHT);
    }
    if (x - 1 < 0) {
      moves.remove(Move.LEFT);
    }
    if (y + 1 > request.getHeight() - 1) {
      moves.remove(Move.DOWN);
    }
    if (y - 1 < 0) {
      moves.remove(Move.UP);
    }
    for (Snake snake : request.getSnakes()) {
      for (int[] cords : snake.getCoords()) {
        if (cords[0] == x + 1 && cords[1] == y) {
          moves.remove(Move.RIGHT);
        }
        if (cords[0] == x - 1 && cords[1] == y) {
          moves.remove(Move.LEFT);
        }
        if (cords[0] == x && cords[1] == y + 1) {
          moves.remove(Move.DOWN);
        }
        if (cords[0] == x && cords[1] == y - 1) {
          moves.remove(Move.UP);
        }
      }
    }


    return moves;
  }

  private Snake getMySnake(MoveRequest request) {
    for (Snake snake : request.getSnakes()) {
      if(snake.getId() == request.getYou()) {
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
