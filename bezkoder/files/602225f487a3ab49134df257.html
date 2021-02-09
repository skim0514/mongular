/*@cc_on
if (@_jscript_version < 9) {
    var _d = document;
    eval("var document = _d");
}
@*/

/*jshint browser:true, strict:false*/
/*global $j:false, $:false, _:false, $F:false, $A:false, $$:false, Namespace:false*/

// firebug compatibility
if (Prototype &&  typeof window.console !== "object") {
    window.console = {
        _lines : [],
        log : function(text) {
            this._lines.push(text);
        },
        dir: function(obj) {
            this.log(obj.inspect());
        },
        show: function() {
            var text = this._lines.join("\n");
            (function() {
                if (typeof document != 'undefined' && $('debugArea'))  {
                    $('debugArea').setValue($F('debugArea') + text + "\n");
                }
            }).defer();
        }
    };
}

var $D = (function(){
    var camelize = function(string){
        return string.replace(/-+(.)?/g, function(match, chr) {
          return chr ? chr.toUpperCase() : '';
        });
    };
    var getDatasetByElement = function(element){
        var sets = {};
        for(var i=0,a=element.attributes,l=a.length;i<l;i++){
            var attr = a[i];
            if( !attr.name.match(/^data-/) ) continue;
            sets[camelize(attr.name.replace(/^data-/,''))] = attr.value;
        }
        return sets;
    };
    return getDatasetByElement;
})();

(function() {
    //avoid malicious location.href property
    var validLocation = location.protocol + '//' + location.host + '/';
    if (location.href.indexOf(validLocation) !== 0) {
        location.href = '//mixi.jp/';
        return;
    }

    function addMethodForMixiGateway(){
        Object.extend(Mixi.Gateway || {}, {
            dump: function(verbose) {
                if (!console) { return; }
                console[verbose ? 'dir' : 'log'](this._params);
            }
        });
    }

    if (Prototype.Version == '1.5.0_rc0') {
        Event.observe(window, 'load', addMethodForMixiGateway);
    } else {
        document.observe('dom:loaded', addMethodForMixiGateway);
    }
})();



function MM_preloadImages() {
  var d=document; if(d.images){ if(!d.MM_p) { d.MM_p=[]; }
  var i,j=d.MM_p.length,a=MM_preloadImages.arguments;
  for(i=0; i<a.length; i++) {
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image(); d.MM_p[j++].src=a[i];}}
  }
}

function MM_openBrWindow(theURL,winName,features) { //v2.0
  window.open(theURL,winName,features);
}

function get_mode() {
    var mode;
    if (window.opera){
        mode = 4;
    }
    else if (navigator.appName == 'Microsoft Internet Explorer') {
        if (navigator.platform == 'MacPPC') {
            mode = 4;
        }
        else {
            mode = 2;
        }
    }
    else if (navigator.userAgent.indexOf('Safari') != -1) {
            mode = 4;
    }
    else if (navigator.appName == 'Netscape') {
        if (navigator.platform == 'MacPPC') {
            mode = 4;
        }
        else {
            mode = 1;
        }
    }
    else if (navigator.userAgent.indexOf('Firefox') != -1) {
        mode = 1;
    }
    else if (navigator.userAgent.indexOf('Netscape') != -1) {
        mode = 1;
    }
    else if (navigator.userAgent.indexOf('Gecko') != -1) {
        mode = 1;
    }
    else {
        mode = 4;
    }
    return mode;
}

function make_tag(str, stag, etag) {
    var mode = get_mode();
    if (mode == 1 || mode == 4) {
        var bl1 = str.value.substring(0, str.selectionStart);
        var bl2 = str.value.substring(str.selectionStart, str.selectionEnd);
        var bl3 = str.value.substring(str.selectionEnd, str.value.length);
        str.value = bl1 + stag + bl2 + etag + bl3;
    }
    else if (mode == 2) {
        str.focus();
        var sel = document.selection.createRange();
        var rang = str.createTextRange();
        rang.moveToPoint(sel.offsetLeft,sel.offsetTop);
        rang.moveEnd("textedit");
        if(rang.text.replace(/\r/g,"").length != 0){
            var las = (str.value.match(/(\r\n)*$/),RegExp.lastMatch.length);
            str.selectionStart = str.value.length - (rang.text.length + las);
            str.selectionEnd = str.selectionStart + sel.text.length;
            str.selectionStart2 = str.value.replace(/\r/g,"").length - (rang.text.replace(/\r/g,"").length + las/2);
            var bl1 = str.value.substring(0, str.selectionStart);
            var bl2 = str.value.substring(str.selectionStart, str.selectionEnd);
            var bl3 = str.value.substring(str.selectionEnd, str.value.length);
            str.value = bl1 + stag + bl2 + etag + bl3;
            str.selectionEnd2 = (str.selectionStart2 + stag.length + bl2.length + etag.length) - str.value.replace(/\r/g,"").length;
            rang.moveStart("character",str.selectionStart2);
            rang.moveEnd("character",str.selectionEnd2);
        }else{
            rang.moveToPoint(sel.offsetLeft,sel.offsetTop);
            rang.text = stag + etag;
            rang.moveStart("character",-(stag.length + etag.length));
        }
        rang.select();
    }
    else if (mode == 3) {
        str.value = stag + str.value + etag;
    }
    else {
        str.value += stag + etag;
    }
    return;
}

function add_tag(str, tag) {
    var stag = '<'  + tag + '>';
    var etag = '</' + tag + '>';
    make_tag(str, stag, etag);
}

function resize_font(str, size) {
    var stag = '<span class="' + size + '">';
    var etag = '</span>';
    make_tag(str, stag, etag);
}

function change_font_color(str, color) {
    var stag = '<span style="color:' + color + '">';
    var etag = '</span>';
    make_tag(str, stag, etag);
}

function swImg(iName,str) {
        document.images[iName].src = str;
}

function swFormImg(name, url) {
    document.getElementById(name).src = url;
}

function is_macie() {
    return (navigator.appName == 'Microsoft Internet Explorer' && navigator.platform == 'MacPPC') ? 1 : 0 ;
}

function setEvent(element, name, func, capture) {
    if (element.addEventListener) { element.addEventListener(name, func, capture);
    } else if (element.attachEvent) { element.attachEvent('on' + name, func); }
}

function addScript(url,charset){
  var script = document.createElement('script');
  script.setAttribute('type', 'text/javascript');
  script.setAttribute('src', url);
  script.setAttribute('charset', charset);
  document.getElementsByTagName('head').item(0).appendChild(script);
}

function addNews(html){
  document.getElementById('member_news_box').innerHTML = html;
}

function setSubmitTrue(element) { window.setTimeout(function() { element.disabled = true; }, 1); }

function setSubmitFalse(element) { window.setTimeout(function() { element.disabled = false; }, 5000); }

function setDisable(elements) {
    var buttons = [];

    for (var i=0; i< elements.length; i++) {
        var element = elements[i];
        if (element.type == 'submit') {
            setSubmitTrue(element);
            setSubmitFalse(element);

            buttons.push(element);
        }
    }

    Event.observe(window, 'unload', function () {
        buttons.each(function (element) {
            element.disabled = false;
        });
    });
}

function disableSubmit(elements) {
    for (var i=0; i<document.forms.length; ++i) {
        if (document.forms[i].onsubmit) continue;
        document.forms[i].onsubmit = function() {
            setDisable(this.elements);
        };
    }
}

function fixThumbnailSize (obj, size) {
    obj.removeAttribute('width');
    obj.removeAttribute('height');

    var ox = obj.width,
        oy = obj.height;

    var ratio = ( ox > oy ) ? ox / size : oy / size;
    if (ratio < 1) ratio = 1;
    obj.width  = Math.floor( ox / ratio );
    obj.height = Math.floor( oy / ratio );
    obj.style.visibility = "visible";
}

if ( !('Mixi' in window) ) {
  window.Mixi = new Object();
}

// js/mixi/util.js
if ( !('Util' in window.Mixi) ) {
  window.Mixi.Util = {};
}
window.Mixi.Util.disableEvent = function(event) {
  if (event.stopPropagation) event.stopPropagation();
  if (event.preventDefault)  event.preventDefault();
  event.cancelBubble = true;
  event.returnValue  = false;
};

// js/mixi/form.js
Mixi.Form = {
  focusedClassName: 'focus',
  bluredClassName:  'blur',
  initialize: function() {
    this.setupColorAction('input');
    this.setupColorAction('textarea');
    this.setupColorAction('select');
  },
  setupColorAction: function (tagName) {
    var elements = document.getElementsByTagName(tagName);
    $A(elements).each(function(element){
      Event.observe(element, 'blur',  Mixi.Form.createBlurCallback(element));
      Event.observe(element, 'focus', Mixi.Form.createFocusCallback(element));
    });
  },
  createBlurCallback: function(element) {
    return function() {
      Element.removeClassName(element, Mixi.Form.focusedClassName);
    };
  },
  createFocusCallback: function(element) {
    return function() {
      Element.addClassName(element, Mixi.Form.focusedClassName);
    };
  }
};
Event.observe(window, 'load', Mixi.Form.initialize.bind(Mixi.Form));

// js/mixi/searchbox.js
Mixi.SearchBox = Class.create();
Object.extend(Mixi.SearchBox.prototype, {
    initialize: function(listId, defaultPageName) {
        var tabs = $(listId).getElementsByTagName('li');
        this.anchors = $A(tabs).collect( function(tab) {
          return tab.getElementsByTagName('a')[0];
        } );
        if (typeof(this.currentName) == 'undefined') {
            this.currentName = defaultPageName;
        }
        this.anchors.each(this.setupAnchorEvent.bind(this));
    },
    setupAnchorEvent: function(anchor) {
        var self = this;
        var tmp = '';
        Event.observe(anchor, 'click', function(event) {
            Mixi.Util.disableEvent(event);
            self.anchors.each(function(element){
                var href = element.getAttribute('href');
                var pageName = href.substr(href.indexOf('#') + 1);
                if (self.currentName == pageName) {
                    var SearchMT      = self.getInputBox(pageName, 'MT');
                    var SearchKeyword = self.getInputBox(pageName, 'keyword');
                    if (typeof(SearchMT) != 'undefined') {
                        tmp = SearchMT.value;
                    } else if (typeof(SearchKeyword) != 'undefined') {
                        tmp = SearchKeyword.value;
                    }
                }
            }),
            self.anchors.each(function(element){
                var href = element.getAttribute('href');
                var pageName = href.substr(href.indexOf('#') + 1);
                var SearchMT      = self.getInputBox(pageName, 'MT');
                var SearchKeyword = self.getInputBox(pageName, 'keyword');
                if(typeof(SearchMT) != 'undefined') {
                    SearchMT.value = tmp;
                } else if (typeof(SearchKeyword) != 'undefined') {
                    SearchKeyword.value = tmp;
                }
                if (element == anchor) {
                    Element.addClassName(element, 'selected');
                    Element.show(pageName);
                    self.currentName = pageName;
                } else {
                    Element.removeClassName(element, 'selected');
                    Element.hide(pageName);
                }
            });
        });
    },
    getInputBox: function(pageName, inputName) {
        var self = this;
        var inputBoxes = $(pageName).getElementsByTagName('input');
        var count = 0;
        while (count < inputBoxes.length) {
            if (inputBoxes[count].name == inputName) {
                var inputBox = $(pageName).getElementsByTagName('input')[count];
            }
            count++;
        }
        return inputBox;
    }
});
Mixi.SearchBBSBox = Class.create();
Object.extend(Mixi.SearchBBSBox.prototype, {
    initialize: function(formId, listId) {
        this.formElement = $(formId);
        var tabs = $(listId).getElementsByTagName('li');
        this.anchors = $A(tabs).collect( function(tab) {
            return tab.getElementsByTagName('a')[0];
        } );
        this.anchors.each(this.setupAnchorEvent.bind(this));
    },
    setupAnchorEvent: function(anchor) {
        var self = this;
        Event.observe(anchor, 'click', function(event) {
            Mixi.Util.disableEvent(event);
            self.anchors.each(function(element){
                var href = element.getAttribute('href');
                var bbsNumber = href.substr(href.indexOf('#') + 1);
                if (element == anchor) {
                    Element.addClassName(element, 'selected');
                    self.formElement.bbs.value = bbsNumber;
                } else {
                    Element.removeClassName(element, 'selected');
                }
            });
        });
    }
});

// js/mixi/navigation.js
/*
 * Mixi.Navigation
 *
 * Dependencies:
 *
 *   Prototype.js ver 1.4 or above.
 *
 * Usage:
 *
 *   <script type="text/javascript" src="/static/js/prototype.js"></script>
 *   <script type="text/javascript" src="/static/js/mixi/navigation.js"></script>
 *   <script type="text/javascript">
 *   <![CDATA[
 *     Mixi.Navigation.setupSubMenus('diary', 'photo', 'video', 'review');
 *   ]]>
 *   </script>
 */

Namespace("jp.mixi.common.navigationbar").define(function(ns){
    var menus = [];
    var COMMUNITY_NAVIGATION_BAR = 'JS_communityLocalNavi';
    var HOME_NAVIGATION_BAR      = 'personalNaviHome';
    var FRIEND_NAVIGATION_BAR    = 'personalNaviFriend';

    var setupMemberNavigationSubMenu = function(targetName){
        $j('#' + targetName + 'Pulldown').mouseover(function(evt){
            var targetElement = $j('#' + targetName + 'SubMenu');
            targetElement.show();
        });
        $j('#' + targetName + 'Pulldown').click(function(evt){
            var targetElement = $j('#' + targetName + 'SubMenu');
            targetElement.show();
        });
        $j('#' + targetName + 'Pulldown').mouseout(function(evt){
            var targetElement = $j('#' + targetName + 'SubMenu');
            targetElement.hide();
        });
        $j('#' + targetName + 'Pulldown>a').click(function(evt){
            evt.preventDefault();
        });
    };

    var setupCommunityNavigationSubMenu = function(targetName){
        var navigationBar = $j('.' + COMMUNITY_NAVIGATION_BAR);
        var menuElement   = navigationBar.find('.JS_' + targetName + 'Menu');
        var targetElement = navigationBar.find('.JS_' + targetName + 'SubMenu');
        menuElement.mouseover(function(evt){
            if(evt.target === menuElement.find('a').get(0)){
                return;
            }
            targetElement.show();
        });
        navigationBar.find('.JS_' + targetName + 'PullDownButton').click(function(evt){
            targetElement.show();
            evt.preventDefault();
        });
        menuElement.mouseleave(function(){
            targetElement.hide();
        });
    };

    ns.provide({
        registerElement : function(element, dataset){
            menus = dataset.menus.split(',');
            _.each(menus, function(name){
                if($j(element).hasClass(COMMUNITY_NAVIGATION_BAR)){
                    setupCommunityNavigationSubMenu(name);
                } else if (element.className == HOME_NAVIGATION_BAR ||
                           element.className == FRIEND_NAVIGATION_BAR){
                    setupMemberNavigationSubMenu(name);
                }
            });
        }
    });
});

/*
 * this method is deprecated.
 */
Mixi.Navigation = {
  menus: new Array(),
  setupSubMenus: function() {
    this.menus = $A(arguments);
    var subMenus = this.menus.map(function(name){ return name + 'SubMenu'; });
    subMenus.each(function(element){ Element.hide(element); });

    this.menus.each(function(name){
      this.setupNavigationSubMenu(name);
    }.bind(this));
  },
  setupNavigationSubMenu: function(targetName) {
    Event.observe(targetName + 'Pulldown','mouseover',function(evt){
        Mixi.Util.disableEvent(evt);

        this.menus.reject(function(name){
            return name == targetName;
        }).each(function(name){
            Element.hide(name + 'SubMenu');
        });

        var targetElement = $(targetName + 'SubMenu');
        Element.show(targetElement);

    }.bindAsEventListener(this));

    Event.observe(targetName + 'Pulldown','mouseout',function(evt){
        Mixi.Util.disableEvent(evt);
        Element.hide(targetName + 'SubMenu');
    }.bindAsEventListener(this));

    Event.observe(targetName + 'Pulldown','click',function(evt){
        if(Event.findElement(evt,'li').id == ( targetName + 'Pulldown' )){
            Mixi.Util.disableEvent(evt);
        }
    }.bindAsEventListener(this));

  }
};

/*
 * this method is deprecated.
 */
Mixi.CommunityNavigation = {
  menus: new Array(),
  setupSubMenus: function() {
    this.menus = $A(arguments);
    var subMenus = this.menus.map(function(name){ return name + 'SubMenu'; });
    subMenus = subMenus.findAll(function(name){ return $(name); });
    if(subMenus.length === 0){ return; }
    subMenus.each(function(element){ Element.hide(element); });

    Event.observe(document, 'click', function(event) {
      var caller = Event.element(event);

      if (caller.nodeName.toLowerCase() == "a") return;
      if (subMenus.any(function(element){ return Element.visible(element); })) {
        subMenus.each(function(element){
          element = $(element);
          Element.hide(element);
        });
        Mixi.Util.disableEvent(event);
      }
    });

    this.menus.each(function(name){
      this.setupNavigationSubMenu(name);
    }.bind(this));
  },
  setupNavigationSubMenu: function(targetName) {
    var triggerElementName = targetName + 'PullDownButton';
    if(!$(triggerElementName)){ return; }

    Event.observe(triggerElementName, 'click', function(event) {
      Mixi.Util.disableEvent(event);
      this.menus.reject(function(name){
        return name == targetName;
      }).each(function(name){
        if($(name + 'SubMenu')){
            Element.hide(name + 'SubMenu');
        }
      });

      var targetElement = $(targetName + 'SubMenu');
      Element.toggle(targetElement);
    }.bind(this));
  }
};


Mixi.Home = {};

Mixi.Home.GadgetObject = Class.create();
Mixi.Home.GadgetObject.cache = {};
Mixi.Home.GadgetObject.findOrCreate = function (element) {
    if( !element.id ) return new Mixi.Home.GadgetObject(element);
    var result = Mixi.Home.GadgetObject.cache[element.id];
    if (! result) {
        result = new Mixi.Home.GadgetObject(element);
        Mixi.Home.GadgetObject.cache[element.id] = result;
    }
    return result;
};
Object.extend(Mixi.Home.GadgetObject.prototype, {
    initialize: function (element) {
        this.element = element;

        var body = element.getElementsByClassName('sectionBody')[0];
        if (! body) {
            body = element.getElementsByClassName('contents')[0];
        }
        this.body = body;

        var header = element.getElementsByClassName('sectionHead')[0];
        if (! header) {
            header = element.getElementsByClassName('heading01')[0];
        }
        this.header = header;

        this.closeButton = header.getElementsByClassName('close')[0];
        this.openButton = header.getElementsByClassName('open')[0];
    },

    isOpened: function () {
        return Element.visible(this.body);
    },

    close: function () {
        if (this.body) {
            new Effect.BlindUp(this.body, {
                duration: 0.3, queue:'end'
            });
        }
        this.openButton.show();
        this.closeButton.hide();
    },

    open: function () {
        if (this.body) {
            new Effect.BlindDown(this.body, {
                duration: 0.3, queue:'end',afterFinish: function(effect){
                    this.body.setStyle({ display:'block' });
                    this.adjustContents();
                }.bind(this)
            });
        }
        this.closeButton.show();
        this.openButton.hide();

    },

    remove: function() {
        delete Mixi.Home.GadgetObject.cache[this.element.id];
        this.element.remove();
    },

    adjustContents: function () {
        ;
    }
});

Mixi.HomeGadget = Class.create();
Object.extend(Mixi.HomeGadget.prototype, {
    initialize: function() {
        Mixi.Gadget.animetion = false;
        Mixi.Gadget.links = [];
        Mixi.Gadget.order_num = [];
        Mixi.Gadget.container = [null, $('bodyContents02'), $('bodySub02')];
        Mixi.Gadget.gadgets = [null,null,null];
        Mixi.Gadget.column_num = [];

        Mixi.Gadget.Anime = new Mixi.GadgetAnime;
        Mixi.Gadget.Anime.build_list();

        Mixi.Gadget.links.each(function(anchor) {
            Event.observe(anchor, 'click',this.buttonEvent.bind(this));
        }.bind(this));

        Mixi.Home.ApplicationFeedbox.createAll();
    },
    buttonEvent : function (event){

        Mixi.Util.disableEvent(event);
        if (Mixi.Gadget.animetion){
            return false;
        }
        Mixi.Gadget.animetion = true;
        var caller  = Event.element(event);
        var href    = caller.getAttribute('href');
        var param_index   = href.indexOf('?');
        var param   = href.substr(param_index + 1);
        var url     = href.substr(0, param_index);
        var gadget_param = [];
        param.split(/&/).each(function (param) {
            var keyvalue = param.split(/=/);
            gadget_param[keyvalue[0]] = keyvalue[1];
        });
        var action  = gadget_param['action'];
        var name    = gadget_param['name'];
        var value   = 1;
        var order = Mixi.Gadget.order_num[name];
        var column = Mixi.Gadget.column_num[name];
        var myAjax  = new Ajax.Request(
            url, {
                method: 'get',
                parameters: param,
                onComplete: function () {}
            }
        );

        var offset;
        switch(action){
        case "up":
            offset = order-value;
            Mixi.Gadget.Anime.move(order, offset);
        break;
        case "down":
            offset = order+value;
            Mixi.Gadget.Anime.move(order, offset);
        break;
        case "close":
            Mixi.Gadget.Anime.close(column, order);
        break;
        case "open":
            Mixi.Gadget.Anime.open(column, order);
        break;
        case "delete":
            Mixi.Gadget.animetion = false;
        break;
        }
    }
});

Mixi.Gadget = Class.create();
Object.extend(Mixi.Gadget.prototype, {
    order: [],
    order_num: [],
    column_num: [],
    Anime: false,
    links: [],
    gadgets: [],
    container: [],
    animetion: false
});

Mixi.GadgetAnime = Class.create();
Object.extend(Mixi.GadgetAnime.prototype, {
    initialize: function() {
    },
    build_list : function (){
        for (j=0;j<Mixi.Gadget.container.length;j++) {
            if (!Mixi.Gadget.container[j]) continue;
            Mixi.Gadget.gadgets[j] = [];
            var order_num = 0;

            var array = $A(Mixi.Gadget.container[j].getElementsByClassName("sectionUtility"));
            array = array.concat($A(Mixi.Gadget.container[j].getElementsByClassName("utility02")));

            array.each(function (node) {
                var buttons = $A(node.getElementsByTagName('a'));
                //set gadget number
                if (buttons[1].getAttribute('href').match(/name=([^&]*)&/)) {
                    Mixi.Gadget.order_num[RegExp.$1] = order_num;
                    Mixi.Gadget.column_num[RegExp.$1] = j;
                    Mixi.Gadget.gadgets[j].push(node.parentNode.parentNode);
                    order_num++;
                }
                buttons.each(function (button) {
                    if (button.getAttribute('href').match(/ajax_edit_home_setting\.pl/)) {
                        Mixi.Gadget.links = Mixi.Gadget.links.concat(button);
                    }
                });
            });
        }
        Mixi.Gadget.animetion = false;
    },
    move : function (from, to) {
        if( to >= 0 && to < Mixi.Gadget.gadgets[1].length ){
            new Effect.Parallel([
                new Effect.BlindUp(Mixi.Gadget.gadgets[1][to], { sync: true})
            ], {
             duration: 0.3, queue: 'end',
             afterFinish: this.change.bind(this, from, to)
            });
            new Effect.Parallel([
                new Effect.BlindDown(Mixi.Gadget.gadgets[1][to], { sync: true})
            ], {
                duration: 0.3,  queue: 'end',
                afterFinish: this.build_list
            });
        }
        else {
            Mixi.Gadget.animetion = false;
        }
    },
    getOneGadget: function (column, order) {
        var element = $(Mixi.Gadget.gadgets[column][order]);
        return new Mixi.Home.GadgetObject.findOrCreate(element);
    },
    close : function (column, order) {
        var gadget = this.getOneGadget(column, order);
        gadget.close();
        Mixi.Gadget.animetion = false;
    },
    open : function (column, order) {
        var gadget = this.getOneGadget(column, order);
        gadget.open();
        Mixi.Gadget.animetion = false;
    },
    change : function (from, to) {
        var from_node = Mixi.Gadget.gadgets[1][from];
        var to_node = Mixi.Gadget.gadgets[1][to];
        if( from < to){
            this.insertAfter(from_node, to_node);
        }else{
            $('bodyContents02').insertBefore(from_node, to_node);
        }
    },
    insertAfter : function (from_node,to_node) {
        var parent=to_node.parentNode;
        var lastChild = Mixi.Gadget.gadgets[1][Mixi.Gadget.gadgets[1].length-1];

        var nextElementSibling = this.nextElementSibling(to_node);

        if (to_node == lastChild && nextElementSibling === null){
            return parent.appendChild(from_node);
        }
        else{
            return parent.insertBefore(from_node,nextElementSibling);
        }
    },
    nextElementSibling: function(node) {
        while(node = node.nextSibling){
            if (node.nodeType == 1){
                return node;
            }
        }
        return null;
    }
});

Mixi.Home.ApplicationFeedbox = Class.create();
Object.extend(Mixi.Home.ApplicationFeedbox, {

    createAll: function() {
        Mixi.Home.ApplicationFeedbox.instances = $$('#bodyContents02 div.appliContentsFeed').collect(function(element) {
            return new Mixi.Home.ApplicationFeedbox(element);
        });
    },

    remove: function(instance) {
        Mixi.Home.ApplicationFeedbox.instances = Mixi.Home.ApplicationFeedbox.instances.without(instance);
    },

    instances: []
});
Object.extend(Mixi.Home.ApplicationFeedbox.prototype, {

    initialize: function(element) {
        this.element = element;
        this.title = this.element.select('.sectionHead h2').first().innerHTML;
        this.deleteButton = this.element.select('.sectionUtility .delete a').first();
        this.yPosition = null;
        this.observeEvents();
    },

    tabs: function() {
        return this.element.select('.appliFeedNavigation a');
    },
    panes: function() {
        return this.element.select('.appliContentsFeedList');
    },

    remove: function() {
        Mixi.Home.GadgetObject.findOrCreate(this.element).remove();
        Mixi.Gadget.Anime.build_list();
        Mixi.Home.ApplicationFeedbox.remove(this);
    },

    switchTab: function(element) {
        element = $(element);
        this.tabs().each(function(tab) { tab.removeClassName('current'); });

        var type = $w(element.className).first();

        element.addClassName('current');
        this.panes().each(function(pane) { pane.hasClassName(type) ? pane.show() : pane.hide(); });
    },

    onSwitchingTabEvent: function(event, tab) {
        Mixi.Util.disableEvent(event);
        this.switchTab(tab);
    },

    onDeleteConfirmed: function(event) {
        new Ajax.Request(this.deleteButton.href, {
                method: 'get',
                onComplete: function(transport) {
                    var status = transport.status || 0;

                    // need to accept 1223 as successful response, as IE translates 204 into 1223.
                    if ((status >= 200 && status < 300) || status == 1223) {
                        new Effect.BlindUp(this.element, {
                                duration: 0.3,
                                afterFinish: this.remove.bind(this)
                            });
                        var urlMixiPrefixSsl = Mixi.Gateway.getParam('url_mixi_prefix_ssl');
                        Mixi.Popup.SmallConfirm.finish(
                            '内容確認',
                            '<strong>\u300c' + this.title + '\u300dを非表示にしました。</strong>',
                            '<p class="supplement01">変更後は<a href="' + urlMixiPrefixSsl + 'list_appli.pl?mode=manage">表示設定</a>から設定できます。</p>',
                            Prototype.emptyFunction,
                            this.yPosition
                        );
                    } else {
                        Mixi.Popup.SmallConfirm.finish(
                            '内容確認',
                            '<strong>\u300c' + this.title + '\u300dを非表示にできませんでした</strong>',
                            '<p class="supplement01">未登録のアプリか、すでにアプリが削除されたのかもしれません。再度読み込むには<a href="#" onclick="window.location.reload();">こちら</a>をクリックしてください</p>',
                            Prototype.emptyFunction,
                            this.yPosition
                        );
                    }

                    this.yPosition = null;
                }.bind(this)
            });
    },

    onClickDeleteButtonEvent: function(event) {
        this.yPosition = { align: "bottom", baseY: Position.cumulativeOffset($(Event.element(event)))[1] };

        Mixi.Util.disableEvent(event);
        var urlMixiPrefixSsl = Mixi.Gateway.getParam('url_mixi_prefix_ssl');
        Mixi.Popup.SmallConfirm.show(
            '内容確認',
            '<strong>\u300c' + this.title + '\u300dを非表示にします。よろしいですか？</strong>',
            '<p class="supplement01">変更後は<a href="' + urlMixiPrefixSsl + 'list_appli.pl?mode=manage">表示設定</a>から設定できます。</p>',
            "はい",
            "いいえ",
            this.onDeleteConfirmed.bind(this),
            this.yPosition
        );
    },

    observeEvents: function() {
        this.tabs().each(function(tab) {
            tab.observe('click', this.onSwitchingTabEvent.bindAsEventListener(this, tab));
        }.bind(this));

        // Remove all handlers Mixi.HomeGadget previously registered
        this.deleteButton.stopObserving('click');
        this.deleteButton.observe('click', this.onClickDeleteButtonEvent.bindAsEventListener(this));
    }

});

Mixi.Home.ImageAdjuster = Class.create();
Mixi.Home.ImageAdjuster.MAX = 50;
Mixi.Home.ImageAdjuster.IMG_BASE = 'https://img.mixi.net';
Mixi.Home.ImageAdjuster.removeAttributes = function(image) {
    image.removeAttribute('width');
    image.removeAttribute('height');
};
Mixi.Home.ImageAdjuster.adjust = function(image, key, resizeMax, doNotExpand) {
    var size = resizeMax || Mixi.Home.ImageAdjuster.MAX;
    image.onload = function () {
        Mixi.Home.ImageAdjuster.removeAttributes(this);

        if (this.width != 1) {
            if (doNotExpand && (this.width <= size)) {
                return;
            } else {
                Mixi.Home.ImageAdjuster.resize(this, size);
            }
        } else {
            Mixi.Home.ImageAdjuster.setNoImage(this, key, size);
        }
    };

    image.onerror = function () {
        Mixi.Home.ImageAdjuster.setNoImage(image, key, size);
    };

    image.src = image.src;
    if ( typeof image.complete !== "undefined" && image.complete && image.onload ) image.onload();
};
Mixi.Home.ImageAdjuster.resize = function (image, size) {
    if (image.width > image.height) {
        image.width = size;
    } else {
        image.height = size;
    }
};
Mixi.Home.ImageAdjuster.setNoImage = function (image, key, size) {
    image.onload = Prototype.emptyFunction;
    image.onerror =  Prototype.emptyFunction;
    image.width = size || Mixi.Home.ImageAdjuster.MAX;
    image.removeAttribute('height');
    image.src = Mixi.Home.ImageAdjuster.noImageOf(key);
};
Mixi.Home.ImageAdjuster.noImageOf = function (key) {
    var path = '/img/basic/common/';
    var image = {
        music: 'noimage_music75.gif',
        photo: 'noimage_photo76.gif',
        video: 'noimage_video76.gif',
        review: 'noimage_review76.gif',
        checked: 'noimage_photo76.gif'
    }[key];
    if (image) {
        return Mixi.Home.ImageAdjuster.IMG_BASE + path + image;
    }
    return Mixi.Home.ImageAdjuster.IMG_BASE + path + 'noimage_logo76_001.gif';
};


Mixi.Home.NewUpdatesAdjuster = Class.create();
Object.extend(Mixi.Home.NewUpdatesAdjuster.prototype, {
    initialize: function (element) {
        if (! element) {
            return;
        }
        this.gadget = Mixi.Home.GadgetObject.findOrCreate(element);

        var instance = this;
        if (Prototype.Browser.IE && ! this.gadget.isOpened()) {
            // IE cannot get image size before rendering.
            this.removeSize();
            this.gadget.adjustContents = function () {
                instance.adjust();
                instance.gadget.adjustContents = Prototype.emptyFunction;
            };
        } else {
            this.adjust();
        }
    },

    getItems: function () {
        if (! this.gadget) {
            return [];
        }
        return $$('p.resize_target');
    },

    removeSize: function () {
        this.getItems().each(function (item) {
            var image = item.getElementsByTagName('img')[0];
            Mixi.Home.ImageAdjuster.removeAttributes(item);
        });
    },

    adjust: function () {
        var instance = this;
        this.getItems().each(function (item) {
            var image = item.getElementsByTagName('img')[0];
            Mixi.Home.ImageAdjuster.adjust(image, item.className.match(/(\s|^)(music|photo|video|review)(\s|$)/)[2]);
        }.bind(this));
    }
});

Mixi.User = Class.create();
Object.extend(Mixi.User, {
    getViewer: function (){
        return this.viewer;
    },
    getOwner: function (){
        return this.owner;
    },
    setViewer: function (viewer){
        this.viewer = new Mixi.User(viewer);
    },
    setOwner: function (owner){
        this.owner = new Mixi.User(owner);
    }
});
Object.extend(Mixi.User.prototype, {
    initialize: function (params){
        if (!params) return;
        for( key in params) {
            this[key] = params[key];
        }
    },
    getId: function (){
        return this.id;
    },
    setId: function (id){
        this.id = id;
    },
    getUserId : function() {
        return this.uid;
    },
    setUserId : function(id) {
        this.uid = id;
    },
    hasRequiredId : function () {
        return this.id  ? true :
               this.uid ? true :
                          false;
    },
    equals : function (user) {
        if (!user)                 { return false; }
        if (!user.hasRequiredId()) { return false; }
        if (!this.hasRequiredId()) { return false; }
        return this.getUserId() == user.getUserId() ||
               this.getId()     == user.getId();
    }
});


Mixi.Header = {};
Mixi.Header.Search = Class.create();
Mixi.Header.Search.IN_MIXI = 'mixi全体から探す';
Mixi.Header.Search.IN_WEB  = 'web全体から探す';
Object.extend(Mixi.Header.Search.prototype, {
    initialize: function (select, input) {
        input.style.color = '#999';
        input.value = Mixi.Header.Search.IN_MIXI;

        input.onfocus = function () {
            if (input.value == this.placeholder()) {
                input.style.color = "#000";
                input.value = "";
            }
        }.bind(this);

        input.onblur = function () {
            if (input.value == "") {
                input.style.color = "#999";
                input.value = this.placeholder();
            }
        }.bind(this);

        select.onchange = function () {
            if ($F(select) == 'web') {
                if (input.value == Mixi.Header.Search.IN_MIXI) {
                    input.value = Mixi.Header.Search.IN_WEB;
                }
            } else {
                if (input.value == Mixi.Header.Search.IN_WEB) {
                    input.value = Mixi.Header.Search.IN_MIXI;
                }
            }
        };

        this.select = select;


        var form = this.select.parentNode;
        form.onsubmit = function () {
            if (input.value == this.placeholder()) {
                input.value = '';
            }
            return true;
        }.bind(this);
    },

    placeholder: function () {
        if ($F(this.select) == 'web') {
            return Mixi.Header.Search.IN_WEB;
        } else {
            return Mixi.Header.Search.IN_MIXI;
        }
    }
});

Mixi.Initializer = null;

(function (ns) {
    var Initializer = function () {
        this._callbacksOf = {};
    };
    Initializer.prototype = {
        add: function (basename, callback) {
            if (!this._callbacksOf[basename]) {
                this._callbacksOf[basename] = [];
            }
            this._callbacksOf[basename].push(callback);
        },

        run: function (basename) {
            (this._callbacksOf[basename] || [])
                .concat(this._callbacksOf['*'] || []).invoke('call');
          }
    };

    ns.Initializer = new Initializer;
})(Mixi);

/*
 * Event.pointer
 */

if (typeof Event.pointer != 'function') {
    Event.pointer = function(event){
        return {
            x:Event.pointerX(event),
            y:Event.pointerY(event)
        };
    }
}

setEvent(window,'load', disableSubmit, 0);
(function () {
    var f = function () {
        Mixi.Initializer.run(location.pathname.replace(/^\//, ''));
    };

    if (Prototype.Version == '1.5.0_rc0') {
        Event.observe(window, 'load', f);
    } else {
        document.observe('dom:loaded', f);
    }

    if (document.execCommand) {
        try {
            document.execCommand('BackgroundImageCache', false, true);
        } catch (e) {
            ;
        }
    }
})();


// support report link
(function() {

    var HiddenElement = Class.create();
    Object.extend(HiddenElement.prototype, {
        initialize: function(element) {
            if (!Object.isElement(element)) {return;}
            this.element = element;
            this.isDisplayed = 0;
        },
        display: function() {
            if (this.isDisplayed) {return;}
            this.element.update(this._getHTMLStringFromComment());
            this.element.show();
            this.isDisplayed = 1;
        },
        _getHTMLStringFromComment: function() {
            return $A(this.element.childNodes)
                .select(function(m){return (m.nodeType==8);})
                .map(function(m){return m.data;}).join('');
        }
    });

    var SupportReportByURILink = Class.create();
    Object.extend(SupportReportByURILink.prototype, {
        initialize: function(supportReportLink) {
            supportReportLink.href = supportReportLink.href.sub('&mode=jsoff', '');
        }
    });

    var SupportReportByPostLink = Class.create();
    Object.extend(SupportReportByPostLink.prototype, {
        initialize: function(supportReportLink) {

          this.hiddenElementList = $$('p.reportLink01').concat($$('div.JS_supportReportGuide')).map(
            function(e) {return new HiddenElement(e)}
          );

           supportReportLink.observe('click', function(event) {
                event.stop();
                this.hiddenElementList.invoke('display');

                this.newHiddenElementList = $$('p.reportLink01').filter(function(item, index){
                  return (item.getStyle('display') == 'none');
                });

                this.newHiddenElementList = this.newHiddenElementList.map(
                  function(e) {return new HiddenElement(e)}
                );

                this.newHiddenElementList.invoke('display');

                $$('div.JS_supportReportGuide').first().scrollTo();
            }.bindAsEventListener(this));

            supportReportLink.href = 'javascript:void(0)';
        }
    });

    Mixi.Initializer.add('*', function() {
        var supportReportLink = $$('.JS_supportReportLink');
        if (!supportReportLink) return;
        supportReportLink.each(function(elm) {
            if (elm.hasClassName('JS_supportReportByPostMode')) {
                new SupportReportByPostLink(elm);
            } else {
                new SupportReportByURILink(elm);
            }
        });
    });
})();
