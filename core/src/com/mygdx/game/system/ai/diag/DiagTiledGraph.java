/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mygdx.game.system.ai.diag;

import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.mygdx.game.system.ai.DungeonUtils;
import com.mygdx.game.system.ai.TiledGraph;

/** A random generated graph representing a flat tiled map.
 * 
 * @author davebaol */
public class DiagTiledGraph extends DefaultIndexedGraph<DiagTiledNode> implements TiledGraph<DiagTiledNode> {
//	public static final int sizeX = 125; // 200; //100;
//	public static final int sizeY = 75; // 120; //60;

	public boolean diagonal;
	public DiagTiledNode startNode;

	public int sizeX, sizeY;
	public DiagTiledGraph (int width, int height) {
		super(width * height);
		sizeX = width;
		sizeY = height;
		this.diagonal = true;
		this.startNode = null;
	}

	public void init (TiledMap tiledMap) {
//		int map[][] = DungeonUtils.generate(sizeX, sizeY, roomCount, roomMinSize, roomMaxSize, squashIterations);
		int map[][] = tiles(tiledMap);
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				nodes.add(new DiagTiledNode(x, y, map[x][y], 4, sizeY));
			}
		}

		// Each node has up to 4 neighbors, therefore no diagonal movement is possible
		for (int x = 0; x < sizeX; x++) {
			int idx = x * sizeY;
			for (int y = 0; y < sizeY; y++) {
				DiagTiledNode n = nodes.get(idx + y);
				if (x > 0) addConnection(n, -1, 0);
				if (y > 0) addConnection(n, 0, -1);
				if (x < sizeX - 1) addConnection(n, 1, 0);
				if (y < sizeY - 1) addConnection(n, 0, 1);
			}
		}
	}

	private int[][] tiles(TiledMap map) {
		return null;
	}

	@Override
	public DiagTiledNode getNode (int x, int y) {
		return nodes.get(x * sizeY + y);
	}

	@Override
	public DiagTiledNode getNode (int index) {
		return nodes.get(index);
	}

	private void addConnection (DiagTiledNode n, int xOffset, int yOffset) {
		DiagTiledNode target = getNode(n.x + xOffset, n.y + yOffset);
		if (target.type == DiagTiledNode.TILE_FLOOR) n.getConnections().add(new DiagTiledConnection(this, n, target));
	}

}
