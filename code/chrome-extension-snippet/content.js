console.log('Content script running');

function setSentenceSnippets(){
    console.log("Running setSentenceSnippets");
    var ptags = document.getElementsByTagName("p");
    for(var i = 0; i < ptags.length; i++){
        processPTag(ptags[i]);
    }
}

function processPTag(pelement){
   var ptext = pelement.innerHTML;
   console.log(ptext);
}

function highlightSnippetContext(snippet){
    var highlightWord = snippet.innerText.trim();
    console.log("Highlighting snippet context for:", highlightWord);
    $(".post-text").mark(highlightWord);
    $("pre").mark(highlightWord);
}

function unhighlightSnippetContext(snippet){
    var highlightWord = snippet.innerText.trim();
    console.log("Unhighlighting snippet context for:", highlightWord);
    $("p").unmark();
    $("pre").unmark();
}

function setCodeSnippets(){
    /* Modify the code spans on StackOverflow to match delimiters */
    /* Wrap code snippets with snippet tags */
    console.log("Running set code snippets");
    var pres = document.getElementsByTagName("pre");
    console.log(pres);
    for(var k = 0; k < pres.length; k++){
        var spans = pres[k].getElementsByTagName("span"); 
        for(var i = 0; i < spans.length; i++){
            var initialSnippet = spans[i].innerHTML;
            console.log("This is the inner text of this span:", initialSnippet);
            var snippetStart = 0;
            for(var j = 0; j < initialSnippet.length; j++){
                /*Add delimeters to if statement below*/
                if(initialSnippet[j] != ' ' && initialSnippet[j] != '\n'){
                    break;
                }
                else{
                    snippetStart++;
                }
            }
            var snippetEnd = snippetStart;
            for(var j = snippetStart; j < initialSnippet.length; j++){
                /*Add delimeters to if statement below */
                if(initialSnippet[j] == ' '){
                    break;
                }
                else{
                    snippetEnd++;
                }
            }

            spans[i].innerHTML = initialSnippet.slice(0, snippetStart) + "<snippet>" + 
                initialSnippet.slice(snippetStart, snippetEnd) + "</snippet>" + initialSnippet.slice(snippetEnd, initialSnippet.length);
        }
    }
}

function modifyCodeSnippets(){
    /* On StackOverflow, code is located in pre tags */
    /* Every seperate 'word' is in a seperate span*/
    /* The previous function wraps every snippet inside a snippet tag.*/
    /* Thus this property can be used to modify mouseover action. */
    console.log("Running modify code snippets");
    var pres = document.getElementsByTagName("pre");
    console.log(pres);
    for(var k = 0; k < pres.length; k++){
        var snippets = pres[k].getElementsByTagName("snippet");
        console.log(snippets);
        for(var i = 0; i < snippets.length; i++){
            if(snippets[i] != undefined){
                snippets[i].onmouseleave = function() {
                    console.log("You left a code span");
                    this.setAttribute("style", "background-color:transparent");
                    unhighlightSnippetContext(this);
                    console.log("Hover data set to false");
                    this.dataset.hover = "false";
                };
                snippets[i].onmouseover = function() {
                    var snippet = this;
                    console.log("You hovered over a code span");
                    snippet.dataset.hover = "true";
                    setTimeout(function(){
                        if(snippet.dataset.hover == "true"){
                            console.log("Highlighting text");
                            snippet.setAttribute("style", "background-color:yellow"); 
                            highlightSnippetContext(snippet);
                        }
                    }, 200);
                };

                snippets[i].onmousedown = function() {
                    /* If the snippet is hovered over, copy on mouse down.*/
                    var snippet = this;
                    if(this.dataset.hover == "true"){
                        console.log(snippet);
                        var selection = document.createRange();
                        selection.selectNode(snippet);
                        window.getSelection().addRange(selection);
                        document.execCommand("copy");
                    }
                };

                snippets[i].onmouseup = function() {
                    /* Clear clipboard on mouse up. */
                    document.getSelection().removeAllRanges();
                }
            }
        }
    }
}   

function addPreButtons() {
    /* Add quality of life buttons to the pre (code) sections*/
    console.log("Running addpreButtons()");

    var pres = document.getElementsByTagName("pre");
    for(var i = 0; i < pres.length; i++){
        pres[i].innerHTML += "<i><img src='"+chrome.extension.getURL("images/copy_32.png")+"'/></i>";
    }

    $("pre img").css({"width" : "16px", "height" : "16px", "float" : "right"});
    $("pre img").click(function() {
        var pre = $(this).parent().parent()[0]
        console.log("You pressed a button that belongs to:", pre);
        var selection = document.createRange();
        selection.selectNode(pre);
        window.getSelection().addRange(selection);
        document.execCommand("copy");
        document.getSelection().removeAllRanges();
    });

}

setSentenceSnippets();
setCodeSnippets();
addPreButtons();
modifyCodeSnippets();