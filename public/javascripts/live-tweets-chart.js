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
            title: {
                enabled: true,
                text: 'Sentiment Score (%)'
            }
        },
        zAxis: {
            title: {
                enabled: true,
                text: 'Tweet'
            }
        },
        legend: {
            layout: 'vertical',
            align: 'left',
            verticalAlign: 'top',
            x: 100,
            y: 70,
            z: 11,
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
                            lineColor: 'rgb(100,100,100)'
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
                    pointFormat: 'Scored {point.y} at {point.x} <br> {point.tweet} <br>'
                }
            }
        },
        series: [
            {
                name: 'Negative',
                color: 'rgba(223, 83, 83, .5)',
                data: []
            },
            {
                name: 'Neutral',
                color: 'rgba(214, 209, 209, .5)',
                data: []
            },
            {
                name: 'Positive',
                color: 'rgba(60, 118, 61, .5)',
                data: []
            }
        ]
    });

});