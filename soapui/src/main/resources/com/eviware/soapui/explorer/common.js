        if (!String.prototype.format) {
            String.prototype.format = function () {
                var args = arguments;
                return this.replace(/{(\d+)}/g, function (match, number) {
                    return typeof args[number] != 'undefined'
                        ? args[number]
                        : match
                        ;
                });
            };
        }

//Class
(function () {
    var initializing = false,
      fnTest = /xyz/.test(function () { xyz; }) ? /\b_super\b/ : /.*/;

    jQuery.Class = function () { };

    jQuery.Class.create = function (prop) {
        var _super = this.prototype;

        initializing = true;
        var prototype = new this();
        initializing = false;

        for (var name in prop) {
            prototype[name] = typeof prop[name] == "function" &&
            typeof _super[name] == "function" && fnTest.test(prop[name]) ?
            (function (name, fn) {
                return function () {
                    var tmp = this._super;
                    this._super = _super[name];
                    var ret = fn.apply(this, arguments);
                    this._super = tmp;

                    return ret;
                };
            })(name, prop[name]) :
            prop[name];
        }
        function Class() {
            if (!initializing && Class.prototype.init)
                return Class.prototype.init.apply(this, arguments);
        }
        Class.prototype = prototype;
        Class.prototype.constructor = Class;
        Class.extend = arguments.callee;
        return Class;
    };

    jQuery.querySelectorAll = function () {
        return jQuery.apply(jQuery, arguments);
    };

    jQuery.querySelector = function () {
        return jQuery.querySelectorAll.apply(jQuery, arguments)[0];
    };

    jQuery.fn.forEach = function (fn) {
        return this.each(function (i) {
            fn(this, i);
        });
    };

    jQuery.fn.attach = function (fn) {
        var attach = fn.attach || (new fn).attach || function () { };
        return this.forEach(function (elem) {
            attach.call(fn, elem);
        });
    };

    jQuery.DOM = buildClass(["prepend", "append", ["before", "insertBefore"],
        ["after", "insertAfter"], "wrap",
        "wrapInner", "wrapAll", "clone", "empty", "remove", "replaceWith",
        ["removeAttr", "removeAttribute"], ["addClass", "addClassName"],
        ["hasClass", "hasClassName"], ["removeClass", "removeClassName"],
        ["offset", "getOffset"]],
      [["text", "Text"], ["html", "HTML"], ["attr", "Attribute"],
        ["val", "Value"], ["height", "Height"], ["width", "Width"],
        ["css", "CSS"]]);

    jQuery.Traverse = buildClass([["children", "getChildElements"],
      ["find", "getDescendantElements"], ["next", "getNextSiblingElements"],
      ["nextAll", "getAllNextSiblingElements"], ["parent", "getParentElements"],
      ["parents", "getAncestorElements"], ["prev", "getPreviousSiblingElements"],
      ["prevAll", "getAllPreviousSiblingElements"],
      ["siblings", "getSiblingElements"], ["filter", "filterSelector"]]);

    jQuery.Events = buildClass([["bind", "addEventListener"],
      ["unbind", "removeEventListener"], ["trigger", "triggerEvent"],
      "hover", "toggle"]);

    jQuery.fn.buildAnimation = function (options) {
        var self = this;

        return {
            start: function () {
                self.animate(options);
            },
            stop: function () {
                self.stop();
            }
        };
    };

    jQuery.Effects = buildClass(["show", "hide", "toggle",
      "buildAnimation", "queue", "dequeue"]);

    jQuery.fn.ajax = jQuery.ajax;

    jQuery.Ajax = buildClass([["ajax", "request"], ["load", "loadAndInsert"],
      ["ajaxSetup", "setup"], ["serialize", "getSerializedString"],
      ["serializeArray", "getSerializedArray"]]);

    function buildClass(methods, getset) {
        var base = {};

        jQuery.each(getset || [], function (i, name) {
            if (!(name instanceof Array))
                name = [name, name];

            methods.push([name[0], "get" + name[1]], [name[0], "set" + name[1]]);
        });

        jQuery.each(methods, function (i, name) {
            var showName = name;

            if (name instanceof Array) {
                showName = name[1];
                name = name[0];
            }

            base[showName] = jQuery.Class.create({
                init: function () {
                    var args = Array.prototype.slice.call(arguments);

                    if (this.constructor == base[showName])
                        this.arguments = args;
                    else
                        return base[showName].prototype.attach.apply(base[showName], args);
                },
                arguments: [],
                attach: function (elem) {
                    var args = arguments.length == 1 ?
                      this.arguments :
                      Array.prototype.slice.call(arguments, 1);

                    if (args.length) {
                        var fn = args[args.length - 1];
                        if (typeof fn == "function") {
                            args[args.length - 1] = function () {
                                var args = Array.prototype.slice.call(arguments);
                                return fn.apply(this, [this].concat(args));
                            };
                        }
                    }

                    return jQuery.fn[name].apply(jQuery(elem), args);
                }
            });
        });

        return base;
    }
})();
