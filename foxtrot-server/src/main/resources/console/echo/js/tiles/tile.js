/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function Queue() {
  this.requests = {};
  this.refreshTime = 5;
  this.timeout = 4000;
}
Queue.prototype.enqueue = function (key, ajaxRequest) {
  console.log("Adding: " + key);
  this.requests[key] = ajaxRequest;
};
Queue.prototype.remove = function (key) {
  console.log("Removing: " + key);
  delete this.requests[key];
};
Queue.prototype.start = function () {
  setInterval($.proxy(this.executeCalls, this), this.refreshTime * 1000);
};
Queue.prototype.executeCalls = function () {
  for (var property in this.requests) {
    if (this.requests.hasOwnProperty(property)) {
      this.requests[property]();
    }
  }
};

function Tile() {}

function TileFactory() {
  this.tileObject = "";
}

function refereshTiles() {
  for (var key in tileData) {
    if (tileData.hasOwnProperty(key)) {
      var a = new TileFactory();
      a.createGraph(tileData[key], $("#"+ key));
    }
  }
}

setInterval(function () {
  refereshTiles();
}, 6000);

function pushTilesObject(object) {
  tileData[object.id] = object;
  var tabName = (object.tileContext.tabName == undefined ? $(".tab .active").attr('id') : object.tileContext.tabName) ;
  var tempObject = {
    "id":convertName(tabName),
    "name": tabName,
    "tileList": tileList
    , "tileData": tileData
  }
  if (tileList.length > 0) {
    var deleteIndex = globalData.findIndex(x => x.id == tabName.trim().toLowerCase().split(' ').join("_"));
    if (deleteIndex >= 0) {
      globalData.splice(deleteIndex, 1);
      globalData.splice(deleteIndex, 0, tempObject);
      //tileList = [];
    }
    else {
      globalData.push(tempObject);
    }
  }
}

TileFactory.prototype.updateTileData = function () {
  var selectedTile = $("#" + this.tileObject.id);
  selectedTile.find(".tile-title").text(this.tileObject.title);
  var tileid = this.tileObject.id;
  this.createGraph(this.tileObject, selectedTile);
  var tileListIndex = tileList.indexOf(this.tileObject.id);
  var tileDataIndex = tileData[this.tileObject.id];
  delete tileData[tileDataIndex.id];
  tileData[this.tileObject.id] = this.tileObject;
}
TileFactory.prototype.createTileData = function (object) {
  var selectedTile = $("#" + object.id);
  selectedTile.find(".tile-title").text(object.title);
  var tileid = object.id;
  var prepareTileData = {};
  prepareTileData[object.id] = object;
  pushTilesObject(object);
}
TileFactory.prototype.getTileFormValue = function (form, modal, object) {
  var tileFormValue = {};
  tileFormValue.title = form.find(".tile-title").val();
  tileFormValue.table = form.find(".tile-table").val();
  tileFormValue.id = form.find(".tileId").val();
  tileFormValue.timeUnit = form.find(".tile-time-unit").val();
  tileFormValue.timeValue = form.find(".tile-time-value").val();
  tileFormValue.chartType = form.find(".tile-chart-type").val();
  //updateTile(tileFormValue, modal);
}

function setConfigValue(object) {
  setTimeout(function(){
    if (currentChartType == "gauge") {
      setGaugeChartFormValues(object);
    }
    else if (currentChartType == "line") {
      setLineChartFormValues(object);
    }
    else if (currentChartType == "trend") {
      setTrendChartFormValues(object);
    }
    else if (currentChartType == "stacked") {
      setStackedChartFormValues(object);
    }
    else if (currentChartType == "radar") {
      setRadarChartFormValues(object);
    }
    else if (currentChartType == "stackedBar") {
      setStackedBarChartFormValues(object);
    }
    else if (currentChartType == "pie") {
      setPieChartFormValues(object);
    }
    else if (currentChartType == "statsTrend") {
      setStatsTrendTileChartFormValues(object);
    }
    else if (currentChartType == "bar") {
      setBarChartFormValues(object);
    }
    else if (currentChartType == "count") {
      setCountChartFormValues(object);
    }
  }, 1000);

}

function newBtnElement(widget, btnRow) {
  console.log('@'+btnRow)
  var columnSize = "";
  var height = "";
  var customClass = "";
  if(widget == "medium") {
    columnSize = "col-md-6";
    height = 500;
    customClass = "medium-btn-color";
  } else {
    columnSize = "col-md-3";
    height= 220;
    customClass = "small-btn-color";
  }
  return "<div class='"+columnSize+" custom-btn-div' style='height:"+height+"px;'><button data-target='#addWidgetModal' class='tile-add-btn tile-add-btn filter-nav-button  custom-add-btn "+customClass+"'onClick='setClicketData(this)'  data-toggle='modal' id='row-" + btnRow + "'>+Add widget</button><div>"
}
// create new div
TileFactory.prototype.createNewRow = function (tileElement) {
  tileElement.addClass("col-md-12"); // add class for div which is full width
  tileElement.addClass("max-height");
  if (panelRow.length == 0) { // initial page
    row = 1;
    panelRow.push({
      widgetType: this.tileObject.tileContext.widgetType
      , id: this.tileObject.id
    });
    tileElement.addClass("row-" + row);
  }
  else { // incremetn row value by one
    panelRow.push({
      widgetType: this.tileObject.tileContext.widgetType
      , id: this.tileObject.id
    });
    row = panelRow.length;
    tileElement.addClass("row-" + row);
  }
  if (this.tileObject.tileContext.widgetType != "full") { // dont add row add button for full widget
    var btnRow = row;
    var newBtn = newBtnElement(this.tileObject.tileContext.widgetType, btnRow);
    tileElement.append(newBtn);
  }

  return tileElement;
}
TileFactory.prototype.updateFilterCreation = function (object) {
  currentChartType = object.tileContext.chartType;
  //removeFilters();
  var tileListIndex = tileList.indexOf(object.id);
  var tileDataIndex = tileData[tileListIndex];
  var selectedTileObject = tileData[object.id];

  var form = $("#sidebar").find("form");
  if (object.tileContext.tableDropdownIndex == undefined) {
    form.find(".tile-table").val(parseInt(tableNameList.indexOf(object.tileContext.table)));
  }
  else {
    form.find(".tile-table").val(parseInt(object.tileContext.tableDropdownIndex));
  }
  $('.tile-table').selectpicker('refresh');


  if (selectedTileObject) {
    setConfigValue(selectedTileObject);
  }

  if (object.tileContext.filters.length > 0) {
    filterRowArray = [];
    for (var invokeFilter = 0; invokeFilter < selectedTileObject.tileContext.filters.length; invokeFilter++) {
      addFilters();
    }
    //setFilters(object);
    setTimeout(function () { //calls click event after a certain time
      setFilters(selectedTileObject.tileContext.filters);
    }, 1000);
  }

}
TileFactory.prototype.updateFilters = function (filters) {
  var instanceVar = this;
  var temp = [];
  instanceVar.tileObject.tileContext.uiFiltersSelectedList = arr_diff(instanceVar.tileObject.tileContext.uiFiltersList, filters)
}
// Filter configuration
TileFactory.prototype.triggerFilter = function (tileElement, object) {
  if(object.tileContext.chartType != "radar" && object.tileContext.chartType != "line") {
    var instanceVar = this;
    tileElement.find(".widget-toolbox").find(".filter").click(function () {
      clearFilterValues();
      var modal = $("#setupFiltersModal").modal('show');
      var fv = $("#setupFiltersModal").find(".filter_values");
      fv.multiselect('refresh');
      var form = modal.find("form");
      form.off('submit');
      form.on('submit', $.proxy(function (e) {
        instanceVar.updateFilters($("#filter_values").val());
        $("#setupFiltersModal").modal('hide');
        e.preventDefault();
      }));
      var options = [];
      if (object.tileContext.uiFiltersList == undefined) return;
      for (var i = 0; i < object.tileContext.uiFiltersList.length; i++) {
        var value = object.tileContext.uiFiltersList[i];
        var index = $.inArray( value, object.tileContext.uiFiltersSelectedList)
        options.push({
          label: value
          , title: value
          , value: value
          , selected: (index == -1 ? true : false)
        });
      }
      fv.multiselect('dataprovider', options);
      fv.multiselect('refresh');
    });
  }
}
// Add click event for tile config icon
TileFactory.prototype.triggerConfig = function (tileElement, object) {
  var instanceVar = this;
  tileElement.find(".widget-toolbox").find(".glyphicon-cog").click(function () {
    object = tileData[object.id];
    isEdit = true;
    editingRow = object.tileContext.row;
    showHideSideBar();
    $('.tile-container').find("#"+object.id).addClass('highlight-tile');
    //$("#addWidgetModal").modal('show');
    $("#sidebar").find(".tileId").val(object.id);
    $("#sidebar").find("#modal-heading").hide();
    $(".vizualization-type").hide();
    $(".chart-type").hide();

    var form = $("#sidebar").find("form");
    form.find(".tile-title").val(object.title);
    form.find("#sidebar-tileId").val(object.id);

    $(".chart-type").val(object.tileContext.chartType)

    if (object.tileContext.tableFields != undefined) { // this is for without console
      currentFieldList = object.tileContext.tableFields;
    }
    else { // with console
      currentFieldList = tableFiledsArray[object.tileContext.table].mappings;
    }

    clickedChartType($(".chart-type"));

    setTimeout(function() { instanceVar.updateFilterCreation(object); }, 1000);
    $(".delete-widget").show();
    $("#delete-widget-divider").show();
    $("#delete-widget-value").val(object.id);
  });
}
TileFactory.prototype.triggerChildBtn = function (tileElement, object) {
  var instanceVar = this;
  tileElement.find(".add-child-btn").find(".child-btn").click(function () {
    $("#addWidgetModal").modal('show');
    $("#addWidgetModal").find(".child-tile").val('true');
    $(".vizualization-type").hide();
    instanceVar.updateFilterCreation(object);
  });
}
// Save action for tile config save button
TileFactory.prototype.saveTileConfig = function (object) {
  $("#tile-configuration").find(".save-changes").click(function () {
    var form = $("#tile-configuration").find("form");
    form.off('submit');
    form.on('submit', $.proxy(function (e) {
      this.getTileFormValue(form, "tile-configuration", object)
      $("#tile-configuration").modal('hide');
      e.preventDefault();
    }, object));
  });
}
TileFactory.prototype.createGraph = function (object, tileElement) {
  if (object.tileContext.chartType == "line") {
    var lineGraph = new LineTile();
    //lineGraph.render(tileElement, object);
    lineGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "radar") {
    tileElement.find(".chart-item").append('<div id="radar-' + object.id + '" style="width:200;height:200"></div>');
    var radarGraph = new RadarTile();
    radarGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "trend") {
    var trendGraph = new TrendTile();
    trendGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "gauge") {
    var gaugeGraph = new GaugeTile();
    gaugeGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "stacked") {
    var stackedGraph = new StackedTile();
    stackedGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "stackedBar") {
    var stackedBarGraph = new StackedBarTile();
    stackedBarGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "pie") {
    var pieGraph = new PieTile();
    pieGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "statsTrend") {
    var statsTrendGraph = new StatsTrendTile();
    statsTrendGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "bar") {
    var barGraph = new BarTile();
    barGraph.getQuery(object);
  }
  else if (object.tileContext.chartType == "count") {
    var countGraph = new CountTile();
    countGraph.getQuery(object);
  }
}
TileFactory.prototype.create = function () {
  var tileElement = $(handlebars("#tile-template", {
    tileId: this.tileObject.id
    , title: this.tileObject.title
  }));

  if(this.tileObject.tileContext.widgetType == "small") {
    smallWidgetCount = smallWidgetCount + 1;
  } else {
    smallWidgetCount = 0;
  }

  if(this.tileObject.tileContext.isnewRow) {
    isNewRowCount = 0;
    smallWidgetCount = 1;
    firstWidgetType = this.tileObject.tileContext.widgetType;
  } else {
    isNewRowCount++;
  }

  if (this.tileObject.tileContext.widgetType == "full") {
    if(isNewRowCount == 1 && previousWidget == 'small') {
      this.tileObject.tileContext.widgetSize = 9;
      tileElement.find(".tile").addClass('full-widget-medium-width');
    } else if(isNewRowCount <= 2 && firstWidgetType != 'pie' && firstWidgetType != "radar") {
      tileElement.find(".tile").addClass((this.tileObject.tileContext.isnewRow ? 'full-widget-max-width' : 'full-widget-min-width'));
      this.tileObject.tileContext.isnewRow ? this.tileObject.tileContext.widgetSize = 12 : this.tileObject.tileContext.widgetSize = 6;

    } else {
      this.tileObject.tileContext.isnewRow = true;
      this.tileObject.tileContext.widgetSize = 12;
    }
  }

  var clickedRow; // clicked row
  if(this.tileObject.tileContext.isnewRow) {
    tileElement = this.createNewRow(tileElement);
    row = this.tileObject.tileContext.row;
  } else {
    row = this.tileObject.tileContext.row;
    if(isNewConsole) {
      tileElement.append(newBtnElement(this.tileObject.tileContext.widgetType), row);
    }
  }

  if (this.tileObject.tileContext.widgetType == "medium") {
    tileElement.find(".tile").addClass('col-sm-6 medium-widget');
    tileElement.find(".tile").height(460);
    tileElement.find(".widget-header").css("background-color", "#fff");
  }
  else if (this.tileObject.tileContext.widgetType == "small") {
    tileElement.find(".tile").addClass('col-sm-3 small-widget');
    tileElement.find(".tile").height(220);
    tileElement.find(".widget-header").css("background-color", "#fff");
    tileElement.find(".widget-header").height(68);
    tileElement.find(".widget-header > .tile-title").css("font-size", "12px");
    tileElement.find(".widget-header > .tile-title").css("width", "112px");
    tileElement.find(".widget-header > .tile-title").addClass("small-widget-title");
  }

  if (this.tileObject.tileContext.chartType == "radar") {
    tileElement.find(".trend-chart").remove();
    tileElement.find(".chart-item").addClass("radar-chart");
  }
  else if (this.tileObject.tileContext.chartType == "line" || this.tileObject.tileContext.chartType == "stacked" || this.tileObject.tileContext.chartType == "stackedBar" || this.tileObject.tileContext.chartType == "pie" || this.tileObject.tileContext.chartType == "statsTrend" || this.tileObject.tileContext.chartType == "bar") {
    /*tileElement.find(".widget-header").append('<div id="' + this.tileObject.id + '-health-text" class="lineGraph-health-text">No Data available</div>');*/
    tileElement.find(".widget-header").append('<div id="' + this.tileObject.id + '-health" style=""></div>');
    tileElement.find(".chart-item").append('<div class="row"><div id="' + this.tileObject.id + '"></div><div class="legend"></div></div>');
    //tileElement.find(".chart-item").append("")
  }
  if (this.tileObject.tileContext.isnewRow) { // new row
    tileElement.insertBefore('.float-clear');
  }
  else { // remove row btn and add new div based on type
    $(tileElement).insertBefore($('.row-' + row).find(".custom-btn-div"));
    $('.row-' + row).find(".custom-btn-div").remove();
    $('.row-' + row).append(newBtnElement(this.tileObject.tileContext.widgetType, this.tileObject.tileContext.row));
  }

  if (this.tileObject.tileContext.widgetType == "small") {
    tileElement.find(".settings").addClass('reduce-filter-size');
    tileElement.find(".widget-timeframe").addClass('reduce-filter-option');
    tileElement.find(".period-select").addClass('reduce-period-select');
    tileElement.find(".settings-icon").addClass('reduce-settings-icon');
    tileElement.find(".filter").hide();
  }
  else if (this.tileObject.tileContext.widgetType == "medium") {
    tileElement.find(".widget-header").addClass('reduce-widget-header-size');
  }

  if(smallWidgetCount == 4) {
    //$(".custom-btn-div").remove();
  }

  var periodSelectElement = tileElement.find(".period-select");

  var timeFrame = this.tileObject.tileContext.timeframe;
  var optionValue = timeFrame+getPeroidSelectString(this.tileObject.tileContext.period);
  var labelString = this.tileObject.tileContext.period;
  var optionLabel = (parseInt(this.tileObject.tileContext.timeframe) <= 1 ? labelString.substring(0, labelString.length - 1)  : labelString);
  $(periodSelectElement).prepend('<option selected value="custom">'+timeFrame+'  '+optionLabel+'</option>');

  this.createGraph(this.tileObject, tileElement);
  this.triggerConfig(tileElement, this.tileObject); // add event for tile config
  this.triggerFilter(tileElement, this.tileObject);
  //this.triggerChildBtn(tileElement,this.tileObject);
  this.createTileData(this.tileObject);
  this.saveTileConfig(this.tileObject); // add event for tile save btn

  previousWidget = this.tileObject.tileContext.widgetType;
};
