case class Rec(left: Option[Rec], right: Option[Rec])
case class Rec2(list: List[Rec2])

case class MutualRec1(mut2: Option[MutualRec2])
case class MutualRec2(mut1: Option[MutualRec1])

case class MutualSelf1(self: List[MutualSelf1], mut2: List[MutualSelf2])
case class MutualSelf2(self: List[MutualSelf2], mut1: List[MutualSelf1])
