package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Map.Entry;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	
	public List<Item> recommenditems(String userId, double lat, double lon){
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getDBConnection();
		Set<String> itemIds = conn.getFavoriteItemIds(userId);
		Map<String, Integer> categories = new HashMap<>();
		for(String id : itemIds) {
			Set<String> category = conn.getCategories(id);
			for(String cat: category) {
				Integer temp = categories.get(cat);
				if(temp == null) {
					categories.put(cat, 1);
				} else {
					categories.put(cat, temp + 1);
				}
			}
		}

		List<Entry<String, Integer>> categoryList = new ArrayList<Entry<String, Integer>>(categories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});
		// Step 3, do search based on category, filter out favorited events, sort by distance
				Set<Item> visitedItems = new HashSet<>();
				for (Entry<String, Integer> category : categoryList) {
					List<Item> items = conn.searchItems(lat, lon, category.getKey());
					List<Item> filteredItems = new ArrayList<>();
					for (Item item : items) {
						if (!itemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
							filteredItems.add(item);
						}
					}
					Collections.sort(filteredItems, new Comparator<Item>() {
						@Override
						public int compare(Item item1, Item item2) {
							// return the increasing order of distance.
							return Double.compare(item1.getDistance(), item2.getDistance());
						}
					});
					visitedItems.addAll(items);
					recommendedItems.addAll(filteredItems);
				}

				return recommendedItems;
		  }

}
