$(function () {
    liveChart = new Highcharts.Chart({
        chart: {
            renderTo: 'container',
            defaultSeriesType: 'scatter',
            zoomType: 'xyz'
        },
        title: {
            text: 'Sentiment Score VS Date Time '
        },
        subtitle: {
            text: 'Source: Twitter API & HP Idol Sentiment Analysis - live tweets about $HPQ.'
        },
        xAxis: {
            type: 'datetime',
            title: {
                enabled: true,
                text: 'Date Time'
            },
            showLastLabel: true
        },
        yAxis: {
            labels: {
                formatter: function () {
                    return (this.value > 0 ? ' + ' : '') + (this.value * 100).toFixed(0) + '%';
                }
            },
            title: {
                enabled: true,
                text: 'Sentiment Score (%)'
            }
        },
        legend: {
            layout: 'vertical',
            align: 'left',
            verticalAlign: 'top',
            x: 100,
            y: 70,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF',
            borderWidth: 1
        },
        plotOptions: {
            scatter: {
                marker: {
                    radius: 5,
                    states: {
                        hover: {
                            enabled: true,
                            lineColor: 'rgb(100,100,100)',
                            hideDelay: 3500
                        }
                    }
                },
                states: {
                    hover: {
                        marker: {
                            enabled: false
                        }
                    }
                },
                tooltip: {
                    headerFormat: '<b>{series.name}</b><br>',
                    pointFormat: 'Scored <b>{point.y}</b> <br> {point.user} <br> {point.tweet} <br>',
                    valueDecimals: 2,
                    xDateFormat: '%Y-%m-%d %HH-%mm-%ss'
                }
            }
        },
        series: [
            {
                name: 'Negative',
                color: 'rgba(223, 83, 83, .7)',
                data: []
            },
            {
                name: 'Neutral',
                color: 'rgba(214, 209, 209, .9)',
                data: []
            },
            {
                name: 'Positive',
                color: 'rgba(60, 118, 61, .7)',
                data: []
            }
        ]
    });

});