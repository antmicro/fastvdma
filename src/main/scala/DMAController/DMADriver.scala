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

package DMAController

import circt.stage.ChiselStage
import DMAConfig._
import DMAUtils.{DMAParseInput, DMALogger}

object DMADriver extends App {
  val config =
    if (args.length == 0) {
      DMALogger.warn("No custom configuration was specified.")
      DMALogger.info("Using default parameters and AXI_AXIL_AXI configuration.")
      new DMAConfig()
    } else {
      DMALogger.info("Applying custom configuration")
      DMAParseInput.parseconfig(args(0)) match {
        case Left(x) => x
        case _ => {
          DMALogger.error("Something went wrong when acquiring DMA Parameters")
          throw new Exception("Invalid configuration")
        }
      }
    }

  ChiselStage.emitSystemVerilogFile(new DMATop(config))
}
