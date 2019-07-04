var inherits = require('inherits')
var Readable = require('readable-stream').Readable

function AbstractSerializer (rdf) {
  this.rdf = rdf
}

AbstractSerializer.prototype.stream = function (inputStream, base, filter) {
  var self = this
  var outputStream = new AbstractSerializer.DataReadStream()

  AbstractSerializer.streamToGraph(inputStream, this.rdf.createGraph()).then(function (graph) {
    self.serialize(graph, function (error, data) {
      if (error) {
        outputStream.emit('error', error)
      } else {
        outputStream.push(data)
        outputStream.emit('end')
      }
    })
  }).catch(function (error) {
    outputStream.emit('error', error)
  })

  return outputStream
}

AbstractSerializer.streamToGraph = function (stream, graph) {
  return new Promise(function (resolve, reject) {
    if (typeof stream !== 'object' || typeof stream.read !== 'function') {
      return resolve(stream)
    }

    stream.on('data', function (triple) {
      graph.add(triple)
    })

    stream.on('end', function () {
      resolve(graph)
    })

    stream.on('error', function (error) {
      reject(error)
    })
  })
}

AbstractSerializer.DataReadStream = function () {
  Readable.call(this, {objectMode: true})

  this._read = function () {
    return 0
  }
}

inherits(AbstractSerializer.DataReadStream, Readable)

module.exports = AbstractSerializer
