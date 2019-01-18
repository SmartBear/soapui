function populateRecentItems() {
    setInterval(function () {
        var html = "";
        var tests = JSON.parse(moduleStarterPageCallback.getRecentItemsAsJSON());
        var len = tests.length;

        for (i = 0; i < len; i++) {
            var id = tests[i].id;
            var name = tests[i].name;
            var icon = tests[i].icon;
            html += "<li class='recent_item'><img class='modelitem_icon' src=\'" + icon + "'><a title=\"" + name + "\" href=\"javascript:void(0)\" onclick=\"moduleStarterPageCallback.openModelItemWithId('" + id + "');\">" + name + "</a></li>"
        }
        var list = document.getElementById("recent-list");
        list.innerHTML = html;


        var checkbox = document.getElementById("on-launch");
        var value = buttonCallback.getUseThisPageAsDefault();
        if (value === null) {
            checkbox.checked = false;
            checkbox.disabled = true;
        } else {
            checkbox.disabled = false;
            checkbox.checked = value;
        }
    }, 3000);
}