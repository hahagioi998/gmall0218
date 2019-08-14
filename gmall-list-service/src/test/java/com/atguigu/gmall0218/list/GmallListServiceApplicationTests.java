package com.atguigu.gmall0218.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Test
	public void contextLoads() {
	}

//	@Autowired
//	JestClient jestClient;
//
//	@Test
//	public void testES() throws IOException {
//		String query="{\n" +
//				"  \"query\": {\n" +
//				"    \"match\": {\n" +
//				"      \"actorList.name\": \"张译\"\n" +
//				"    }\n" +
//				"  }\n" +
//				"}";
//		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie_type_chn").build();
//
//		SearchResult result = jestClient.execute(search);
//
//		List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);
//
//		for (SearchResult.Hit<HashMap, Void> hit : hits) {
//			HashMap source = hit.source;
//			System.out.println("source = " + source);
//		}

	//}
}
