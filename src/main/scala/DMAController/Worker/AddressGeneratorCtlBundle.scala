/*
Copyright (C) 2019-2021 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.Worker

import chisel3._

class AddressGeneratorCtlBundle(val addrWidth : Int) extends Bundle{
  val start = Input(Bool())
  val busy = Output(Bool())
  val startAddress = Input(UInt(addrWidth.W))
  val lineLength = Input(UInt(addrWidth.W))
  val lineCount = Input(UInt(addrWidth.W))
  val lineGap = Input(UInt(addrWidth.W))
}
