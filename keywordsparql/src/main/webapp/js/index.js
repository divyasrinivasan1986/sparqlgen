$(function() {
	/** word2vec part **/
	 'use strict';
	  /**********
	   * config */
	  var NUM_TO_SHOW = 10;
	 
	  /*************
	   * constants */
	  var WORDS = Object.keys(wordVecs);
	
	$("#textbox").keypress(function(event) {
		if (event.which == 13) {
			$("#send").click();
			event.preventDefault();
		}
	});
	$("#send").click(function() {
		var simWords;
		var word2vecmap = {};
		var newMsg = $("#textbox").val();

		$("#textbox").val("");
		var words = newMsg.split(" ");
		for (var i = 0; i < words.length; i++) {
			simWords = Word2VecUtils.findSimilarWords(NUM_TO_SHOW, words[i]);
			if(!(simWords[0]==false))
				word2vecmap[words[i]]=simWords;
			else if(simWords[0]==false){
				var simword = [[words[i],1]];
				word2vecmap[words[i]]=simword;
			}
			//word2vecmap.set(words[i],simWords);
		}
		var maptoobj=function strMapToObj(strMap) {
		    let obj = Object.create(null);
		    for (let [k,v] of strMap) {
		        // We don’t escape the key '__proto__'
		        // which can cause problems on older engines
		        obj[k] = v;
		    }
		    return obj;
		}
		var mapToJson=function strMapToJson(strMap) {
		    return JSON.stringify(maptoobj(strMap));
		}
		var prevMsg = $("#container").html();
		console.log(prevMsg.length);
		if (prevMsg.length != 6) {
			prevMsg = prevMsg + "<br>";
		}
		//searchViaAjax();
		var data = {}
		data["userId"] = "1104ea5f-ce7b-4211-8675-e880b9bd0ec7"; //Need to geenrate some ID for Uniqueness.
		data["messageType"] = "text";
		data["requestContent"]=[{"text":newMsg,"wordMap":word2vecmap}];
		
		$.ajax({
			type : "POST",
			dataType: 'text',
			data: JSON.stringify(data),
			 headers: {
	                'Accept': 'application/json; charset=utf-8',
	                'Content-Type': 'application/json; charset=utf-8'
	         },
	         url: '/sparqlgen', //Need to debug how to read data: in Spring. Passing as command param is not right.
			
			timeout : 100000,
			success : function(data) {
				console.log("SUCCESS: ", data);
				var prevMsg = $("#container").html();
				console.log(prevMsg.length);
				if (prevMsg.length != 6) {
					prevMsg = prevMsg + "<br>";
				}
				var obj = JSON.parse(data);
				console.log(obj);
				for(var i=0; i<obj.length; i++){
				var displayText="<div class='card'>";
				var elementName="Open Link in DBpedia";
				if (typeof obj[i].thumbnail == "undefined")
					console.log("Thumbnail is not present");
				else
					displayText += "<img src=" +  obj[i].thumbnail +" height='200' width='200'/><br>";
				if (typeof obj[i].comment == "undefined")
					console.log("Comment is not present");
				else
					displayText += obj[i].comment +"<br>";
				if (typeof obj[i].URI == "undefined")
					console.log("URI is not present");
				else{
					var link=obj[i].URI;
					console.log(link);
					displayText +=elementName.link(link) + "-->For reference,URL=" + link;
				}
				displayText +="</div>"
				}
				$("#container").html(displayText);
				$("#container").scrollTop($("#container").prop("scrollHeight"));
			},
			error : function(e) {
				console.log("ERROR: ", e);
			
			},
			done : function(e) {
				//
				console.log("DONE");
				//window.alert("DONE");
			}
		});
		
		$("#container").html("Results for '"+newMsg+"'");
		$("#container").scrollTop($("#container").prop("scrollHeight"));
	});
});
