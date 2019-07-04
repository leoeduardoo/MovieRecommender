var concatStream = require('concat-stream')
var inherits = require('inherits')
var Readable = require('readable-stream').Readable

function AbstractParser (rdf) {
  this.rdf = rdf
}

AbstractParser.prototype.parse = function (data, callback, base, filter, graph) {
  var self = this

  graph = graph || self.rdf.createGraph()

  var pushTriple = function (triple) {
    graph.add(triple)
  }

  return new Promise(function (resolve, reject) {
    self.process(data, pushTriple, base, filter).then(function() {
      // callback API
      if (callback) {
        process.nextTick(function () {
            callback(null, graph)
        })
      }

      // Promise API
      resolve(graph)
    }).catch(function(error) {
      if (callback) {
        process.nextTick(function () {
            callback(error)
        })
      }
    
      reject(error)
    })
  })
}

AbstractParser.prototype.stream = function (inputStream, base, filter) {
  var self = this

  var outputStream = new AbstractParser.TripleReadStream()

  AbstractParser.streamToData(inputStream).then(function (data) {
    self.process(data, function (triple) {
      outputStream.push(triple)
    }, base, filter, function (error) {
      if (error) {
        outputStream.emit('error', error)
      } else {
        outputStream.emit('end')
      }
    })
  }).catch(function (error) {
    outputStream.emit('error', error)
  })

  return outputStream
}

AbstractParser.streamToData = function (stream) {
  return new Promise(function (resolve, reject) {
    if (typeof stream !== 'object' || typeof stream.read !== 'function') {
      return resolve(stream)
    }

    stream.on('error', function (error) {
      reject(error)
    })

    stream.pipe(concatStream(function (data) {
      resolve(data)
    }))
  })
}

AbstractParser.TripleReadStream = function () {
  Readable.call(this, {objectMode: true})

  this._read = function () {
    return 0
  }
}

inherits(AbstractParser.TripleReadStream, Readable)

module.exports = AbstractParser
