const path = require('path');

module.exports = {
    entry: './src/index.tsx',
    mode: 'development',
    devServer: {
        port: 8000,
        historyApiFallback: true,
        compress: false,
        proxy: [
          {
            context: ['/api'],
            target: 'http://localhost:8080',
            onProxyRes: (proxyRes, req, res) => {
              console.log('onProxyRes');
              proxyRes.on('close', () => {
                console.log('on proxyRes close');
                if (!res.writableEnded) {
                  res.end();
                }
              });
              res.on('close', () => {
                console.log('on res close');
                proxyRes.destroy();
              });
            },
          },
        ],
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.(png|svg|jpg|jpeg|gif|ico)$/,
                use: ['file-loader?name=[name].[ext]'],
                exclude: /node_modules/
            },
            {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
                exclude: /node_modules/
            },
        ],
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js'],
    },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist'),
    },
};