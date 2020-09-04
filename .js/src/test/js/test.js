const KF = require("../../../target/scala-2.13/kfoundation-scala-fastopt.js");
const R = KF.ValueReadWriters;

const A_RW = R.ofObject("A", {
    "a1": R.NUMBER,
    "a2": R.STRING
});

const B_RW = R.ofObject("A", {
    "b1": R.BOOLEAN
})

const C_RW = R.ofObject("C", {
    "c1": A_RW,
    "c2": B_RW
})

let input = "C[c1=A[a1=123.45 a2=\"abc\"] c2=B[b1=false]]"
let deserializer = new KF.K4ObjectDeserializer(input)

let c = C_RW.read(deserializer)
console.log(c)