/*
 * Prototype.js 1.7未満を読み込んだページでJSON.stringifyを正常に動作させるhack
 */
(function(){
    var existArrayToJSON = (Prototype &&
                            /^1\.6.*/.test(Prototype.Version) &&
                            typeof Array.prototype.toJSON === 'function');

    if (window.JSON && typeof window.JSON.stringify === 'function') {
        var originalStringify = window.JSON.stringify;

        window.JSON.stringify = function(value, replacer, space){
            /* prototype.js 1.7未満によりArray.prototype.toJSONが定義されていると
             * arrayが二重に引用符で囲まれてしまうため、
             * JSON.stringifyの前にtoJSONを消し、終わったら元に戻す
             * */
            if (existArrayToJSON) {
                var originalArrayToJSON = Array.prototype.toJSON;
                delete Array.prototype.toJSON;
                var jsonString = originalStringify(value, replacer, space);
                Array.prototype.toJSON = originalArrayToJSON;
                return jsonString;
            } else {
                return originalStringify(value, replacer, space);
            }
        };
    }
}());
