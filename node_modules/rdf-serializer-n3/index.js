var rdf = require('rdf-ext')
var util = require('util')
var AbstractSerializer = require('rdf-serializer-abstract')
var N3 = require('n3')

function N3Serializer (options) {
  this.options = options || {}
  this.options.usePrefixMap = 'usePrefixMap' in this.options ? this.options.usePrefixMap : true

  AbstractSerializer.call(this, rdf)
}

util.inherits(N3Serializer, AbstractSerializer)

N3Serializer.prototype.serialize = function (graph, done) {
  var self = this

  return new Promise(function (resolve, reject) {
    done = done || function () {}

    var writerOptions = {}
    var writer

    if (self.options.usePrefixMap) {
      writerOptions.prefixes = {}

      // TODO: where do we get the right RDF Environment from?
      Object.keys(rdf.prefixes).forEach(function (prefix) {
        if (typeof rdf.prefixes[prefix] !== 'string') {
          return
        }

        writerOptions.prefixes[prefix] = rdf.prefixes[prefix]
      })
    }

    writer = N3.Writer(writerOptions)

    var createN3Node = function (node) {
      if (node.interfaceName.toString() === 'NamedNode') {
        return node.nominalValue
      } else if (node.interfaceName.toString() === 'BlankNode') {
        return '_:' + node.nominalValue
      } else {
        if (node.language) {
          return '"' + node.nominalValue + '"@' + node.language
        } else if (node.datatype && !node.datatype.equals('http://www.w3.org/2001/XMLSchema#string')) {
          return '"' + node.nominalValue + '"^^' + node.datatype.nominalValue
        } else {
          return '"' + node.nominalValue + '"'
        }
      }
    }

    graph.forEach(function (triple) {
      writer.addTriple(createN3Node(triple.subject), createN3Node(triple.predicate), createN3Node(triple.object))
    })

    writer.end(function (error, result) {
      if (error) {
        done(error)
        reject(error)
      } else {
        done(null, result)
        resolve(result)
      }
    })
  })
}

// add singleton methods to class
var instance = new N3Serializer()

for (var property in instance) {
  N3Serializer[property] = instance[property]
}

module.exports = N3Serializer
