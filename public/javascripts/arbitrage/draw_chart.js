var draw = function(data) {
    // Use UTC
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    // Create the chart
    $('#container').highcharts('StockChart', {
        rangeSelector : {
            selected : 1
        },

        title : {
            text : 'OkCoin to CoinBase Margin'
        },

        series : [{
            name : 'OkCoin to CoinBase Margin',
            data : data,
            marker : {
                enabled : true,
                radius : 3
            },
            shadow : true,
            tooltip : {
                valueDecimals : 2
            }
        }]
    });
}
