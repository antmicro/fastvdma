/*
Copyright (C) 2019-2023 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.CSR

import DMAUtils.DMAModule
import DMAController.DMADriver
import chisel3._
import DMAController.DMAConfig._

class StatusCSR(dmaConfig: DMAConfig) extends DMAModule(dmaConfig){
  val io = IO(new Bundle{
    val csr = Flipped(new CSRRegBundle(dmaConfig.controlDataWidth))
    val value = Input(UInt(dmaConfig.controlDataWidth.W))
  })

  val reg = RegNext(io.value)

  io.csr.dataIn := reg
}

object StatusCSR{
  def apply(status : UInt, csrCtl : CSRRegBundle, dmaConfig: DMAConfig): Unit = {
    val csr = Module(new StatusCSR(dmaConfig))

    csr.io.csr <> csrCtl

    csr.io.value := status
  }
}
